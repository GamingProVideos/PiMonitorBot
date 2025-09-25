package com.gamingprovids.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.Message;

import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class AutoReporter {
    private static Timer timer;
    private Message lastReportMessage = null;
    private final JDA jda;

    public AutoReporter(JDA jda) {
        this.jda = jda;
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown Host";
        }
    }

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
                        .addField("CPU Temperature", String.format("%.1f °C", cpuTemp), true)
                        .addField("GPU Temperature", String.format("%.1f °C", gpuTemp), true)
                        .addField("Fan Speed", (fanPercent >= 0 ? String.format("%.0f%%", fanPercent) : "Unknown"), true)
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

                // Check for updates every cycle
                new UpdateChecker(jda).checkForUpdates();
            }
        }, 0, Config.getIntervalMinutes() * 60 * 1000L);
    }

    public static void restart(JDA jda) {
        if (timer != null) timer.cancel();
        new AutoReporter(jda).start();
    }
}
