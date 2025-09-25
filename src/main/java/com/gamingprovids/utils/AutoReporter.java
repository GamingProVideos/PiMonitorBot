package com.gamingprovids.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class AutoReporter {
    private static Timer timer;
    private static Message lastReportMessage = null;
    private final JDA jda;

    public AutoReporter(JDA jda) {
        this.jda = jda;
    }

    /** Starts the auto-reporting loop */
    public void start() {
        TextChannel channel = jda.getTextChannelById(Config.getChannelId());
        if (channel == null) return;

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                double cpuTemp = PiUtils.getCpuTemperature();
                double gpuTemp = PiUtils.getGpuTemperature();
                double fanPercent = PiUtils.getFanPercentage();
                String hostname = getHostName();

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("🌡️ Auto Report")
                        .addField("Hostname", hostname, false)
                        .addField("CPU Temperature", cpuTemp >= 0 ? String.format("%.1f °C", cpuTemp) : "Unknown", true)
                        .addField("GPU Temperature", gpuTemp >= 0 ? String.format("%.1f °C", gpuTemp) : "Unknown", true)
                        .addField("Fan Speed", fanPercent >= 0 ? String.format("%.0f%%", fanPercent) : "Unknown", true)
                        .setColor(cpuTemp >= Config.getWarnThreshold() ? Color.RED : Color.GREEN);

                if (cpuTemp > 0) {
                    if (lastReportMessage == null) {
                        channel.sendMessageEmbeds(embed.build()).queue(msg -> lastReportMessage = msg);
                    } else {
                        lastReportMessage.editMessageEmbeds(embed.build()).queue();
                    }

                    if (cpuTemp >= Config.getWarnThreshold()) {
                        EmbedBuilder warnEmbed = new EmbedBuilder()
                                .setTitle("⚠️ WARNING")
                                .setDescription(String.format("Pi CPU Temp is high! (%.1f °C)", cpuTemp))
                                .setColor(Color.RED);
                        channel.sendMessageEmbeds(warnEmbed.build()).queue();
                    }
                }

                // Check for updates
                new UpdateChecker(jda).checkForUpdates();
            }
        }, 0, Config.getIntervalMinutes() * 60 * 1000L);
    }

    /** Restart the auto-reporter with a new JDA instance */
    public static void restart(JDA jda) {
        if (timer != null) timer.cancel();
        new AutoReporter(jda).start();
    }

    /** Helper to get the hostname of the Pi */
    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown Host";
        }
    }
}
