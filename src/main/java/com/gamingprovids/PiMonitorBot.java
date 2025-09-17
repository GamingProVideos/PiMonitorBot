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

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PiMonitorBot extends ListenerAdapter {

    // --- CONFIG ---
    private static final Dotenv dotenv = Dotenv.load();

    private static final String TOKEN = dotenv.get("BOT_TOKEN");
    private static final long GUILD_ID = Long.parseLong(dotenv.get("GUILD_ID"));
    private static final long CHANNEL_ID = Long.parseLong(dotenv.get("CHANNEL_ID"));

    // Load dynamic settings from .env
    private static int intervalMinutes = Integer.parseInt(dotenv.get("INTERVAL_MINUTES", "5"));
    private static double warnThreshold = Double.parseDouble(dotenv.get("WARN_THRESHOLD", "70.0"));

    private Timer timer;

    public static void main(String[] args) throws LoginException {
        JDABuilder.createDefault(TOKEN)
                .addEventListeners(new PiMonitorBot())
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
                String fanStr = (fanPercent >= 0) ? String.format("%.0f%%", fanPercent) : "Unknown";

                event.reply(String.format("🌡️ Pi Temp: %.1f °C\n💨 Fan Speed: %s", tempC, fanStr))
                        .queue();
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
                event.reply(String.format(
                        "📊 Current Settings:\n- Interval: %d minutes\n- Warning Threshold: %.1f °C",
                        intervalMinutes, warnThreshold
                )).queue();
            }
        }
    }

    // --- Auto-reporting ---
    private void startAutoReporting(TextChannel channel) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                double tempC = getPiTemperature();
                double fanPercent = getFanPercentage();
                String fanStr = (fanPercent >= 0) ? String.format("%.0f%%", fanPercent) : "Unknown";

                if (tempC > 0) {
                    channel.sendMessage(
                            String.format("🌡️ Auto Report: Pi Temp %.1f °C | 💨 Fan %s", tempC, fanStr)
                    ).queue();

                    if (tempC >= warnThreshold) {
                        channel.sendMessage(
                                String.format("⚠️ WARNING: Pi Temp is high! (%.1f °C)", tempC)
                        ).queue();
                    }
                }
            }
        }, 0, intervalMinutes * 60 * 1000L);
    }

    private void restartAutoReporting(TextChannel channel) {
        if (timer != null) {
            timer.cancel();
        }
        startAutoReporting(channel);
    }

    // --- System reading ---
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
