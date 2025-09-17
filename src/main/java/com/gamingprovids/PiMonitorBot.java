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
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PiMonitorBot extends ListenerAdapter {

    private static final Dotenv dotenv = Dotenv.load();

    private static final String TOKEN = dotenv.get("BOT_TOKEN");
    private static final long GUILD_ID = Long.parseLong(dotenv.get("GUILD_ID"));
    private static final long CHANNEL_ID = Long.parseLong(dotenv.get("CHANNEL_ID"));

    private static int intervalMinutes = Integer.parseInt(dotenv.get("INTERVAL_MINUTES", "5"));
    private static double warnThreshold = Double.parseDouble(dotenv.get("WARN_THRESHOLD", "70.0"));

    private Timer timer;

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
                    Commands.slash("status", "Show current settings")
            ).queue();
        }

        TextChannel channel = event.getJDA().getTextChannelById(CHANNEL_ID);
        if (channel != null) {
            startAutoReporting(channel);
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
                        .setColor(Color.BLUE);
                event.replyEmbeds(embed.build()).queue();
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
}
