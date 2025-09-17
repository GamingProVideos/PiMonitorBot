package com.gamingprovids;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class PiMonitorBot extends ListenerAdapter {

    private static final Dotenv dotenv = Dotenv.load();

    private static final String TOKEN = dotenv.get("BOT_TOKEN");
    private static final long GUILD_ID = Long.parseLong(dotenv.get("GUILD_ID"));
    private static final long CHANNEL_ID = Long.parseLong(dotenv.get("CHANNEL_ID"));

    private static int intervalMinutes = Integer.parseInt(dotenv.get("INTERVAL_MINUTES", "5"));
    private static double warnThreshold = Double.parseDouble(dotenv.get("WARN_THRESHOLD", "70.0"));

    // 🔹 Version control
    private static final String CURRENT_VERSION = "1.0.0"; // bump manually when releasing
    private static final String LATEST_VERSION_URL =
            "https://raw.githubusercontent.com/GamingProVideos/PiMonitorBot/master/VERSION";

    private Timer timer;
    private Message lastUpdateMessage = null;

    public static void main(String[] args) throws LoginException {
        JDABuilder.createDefault(TOKEN)
                .addEventListeners(new PiMonitorBot())
                .setActivity(Activity.playing("Temp Monitor"))
                .build();
    }

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("✅ Bot is online!");

        Guild guild = event.getJDA().getGuildById(GUILD_ID);
        if (guild != null) {
            guild.updateCommands().addCommands(
                    Commands.slash("temp", "Show current Raspberry Pi temperature and fan speed"),
                    Commands.slash("setinterval", "Set auto-report interval in minutes")
                            .addOption(OptionType.INTEGER, "minutes", "Interval in minutes", true),
                    Commands.slash("setwarn", "Set warning threshold in °C")
                            .addOption(OptionType.NUMBER, "temperature", "Temperature in °C", true),
                    Commands.slash("status", "Show current settings"),
                    Commands.slash("checkupdate", "Check if a new version of PiMonitorBot is available")
            ).queue();
        }

        TextChannel channel = event.getJDA().getTextChannelById(CHANNEL_ID);
        if (channel != null) {
            startAutoReporting(channel);
            checkForUpdates(channel); // 🔹 Check for updates at startup
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "temp" -> {
                double tempC = getPiTemperature();
                double fanPercent = getFanPercentage();

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("🌡️ Raspberry Pi Status")
                        .addField("Temperature", String.format("%.1f °C", tempC), true)
                        .addField("Fan Speed", (fanPercent >= 0 ? String.format("%.0f%%", fanPercent) : "Unknown"), true)
                        .setColor(tempC >= warnThreshold ? Color.RED : Color.GREEN);

                event.replyEmbeds(embed.build()).queue();
            }
            case "setinterval" -> {
                int minutes = event.getOption("minutes").getAsInt();
                if (minutes >= 1) {
                    intervalMinutes = minutes;
                    event.reply("✅ Auto-report interval set to " + intervalMinutes + " minutes.").queue();
                    TextChannel channel = event.getJDA().getTextChannelById(CHANNEL_ID);
                    if (channel != null) restartAutoReporting(channel);
                } else {
                    event.reply("⚠️ Interval must be at least 1 minute.").setEphemeral(true).queue();
                }
            }
            case "setwarn" -> {
                double threshold = event.getOption("temperature").getAsDouble();
                warnThreshold = threshold;
                event.reply("✅ Warning threshold set to " + warnThreshold + " °C.").queue();
            }
            case "status" -> {
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("📊 Current Settings")
                        .addField("Interval", intervalMinutes + " minutes", true)
                        .addField("Warning Threshold", warnThreshold + " °C", true)
                        .addField("Bot Version", CURRENT_VERSION, true)
                        .setColor(Color.BLUE);
                event.replyEmbeds(embed.build()).queue();
            }
            case "checkupdate" -> {
                TextChannel channel = event.getJDA().getTextChannelById(CHANNEL_ID);
                if (channel != null) {
                    checkForUpdates(channel);
                    event.reply("🔍 Checking for updates...").setEphemeral(true).queue();
                }
            }
        }
    }

    private void startAutoReporting(TextChannel channel) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                double tempC = getPiTemperature();
                double fanPercent = getFanPercentage();

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("🌡️ Auto Report")
                        .addField("Temperature", String.format("%.1f °C", tempC), true)
                        .addField("Fan Speed", (fanPercent >= 0 ? String.format("%.0f%%", fanPercent) : "Unknown"), true)
                        .setColor(tempC >= warnThreshold ? Color.RED : Color.GREEN);

                if (tempC > 0) {
                    channel.sendMessageEmbeds(embed.build()).queue();

                    if (tempC >= warnThreshold) {
                        EmbedBuilder warnEmbed = new EmbedBuilder()
                                .setTitle("⚠️ WARNING")
                                .setDescription(String.format("Pi Temp is high! (%.1f °C)", tempC))
                                .setColor(Color.RED);
                        channel.sendMessageEmbeds(warnEmbed.build()).queue();
                    }
                }

                // 🔹 Check for updates periodically (every auto-report cycle)
                checkForUpdates(channel);
            }
        }, 0, intervalMinutes * 60 * 1000L);
    }

    private void restartAutoReporting(TextChannel channel) {
        if (timer != null) timer.cancel();
        startAutoReporting(channel);
    }

    private double getPiTemperature() {
        String path = "/sys/class/thermal/thermal_zone0/temp";
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            if (line != null) return Integer.parseInt(line) / 1000.0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1.0;
    }

    private double getFanPercentage() {
        String curPath = "/sys/class/thermal/cooling_device0/cur_state";
        String maxPath = "/sys/class/thermal/cooling_device0/max_state";
        try (BufferedReader curReader = new BufferedReader(new FileReader(curPath));
             BufferedReader maxReader = new BufferedReader(new FileReader(maxPath))) {

            String curLine = curReader.readLine();
            String maxLine = maxReader.readLine();

            if (curLine != null && maxLine != null) {
                int cur = Integer.parseInt(curLine);
                int max = Integer.parseInt(maxLine);
                if (max > 0) return (cur / (double) max) * 100.0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1.0;
    }

    private void checkForUpdates(TextChannel channel) {
        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(LATEST_VERSION_URL).openConnection();
                conn.setRequestProperty("User-Agent", "PiMonitorBot");

                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String latestVersion = in.readLine().trim();

                    if (latestVersion != null && !CURRENT_VERSION.equalsIgnoreCase(latestVersion)) {
                        EmbedBuilder updateEmbed = new EmbedBuilder()
                                .setTitle("🔄 Update Available")
                                .setDescription("Running version **" + CURRENT_VERSION + "**\nLatest version is **" + latestVersion + "**\n\nUpdate here: [GitHub Repo](https://github.com/GamingProVideos/PiMonitorBot)")
                                .setColor(Color.ORANGE);

                        if (lastUpdateMessage == null) {
                            channel.sendMessageEmbeds(updateEmbed.build()).queue(msg -> lastUpdateMessage = msg);
                        } else {
                            lastUpdateMessage.editMessageEmbeds(updateEmbed.build()).queue();
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ Failed to check for updates: " + e.getMessage());
            }
        }).start();
    }
}
