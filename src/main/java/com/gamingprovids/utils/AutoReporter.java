package com.gamingprovids.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class AutoReporter {
    private static Timer timer;
    private final JDA jda;

    public AutoReporter(JDA jda) {
        this.jda = jda;
    }

    public void start() {
        TextChannel channel = jda.getTextChannelById(Config.getChannelId());
        if (channel == null) return;

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                double tempC = PiUtils.getPiTemperature();
                double fanPercent = PiUtils.getFanPercentage();

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("🌡️ Auto Report")
                        .addField("Temperature", String.format("%.1f °C", tempC), true)
                        .addField("Fan Speed", (fanPercent >= 0 ? String.format("%.0f%%", fanPercent) : "Unknown"), true)
                        .setColor(tempC >= Config.getWarnThreshold() ? Color.RED : Color.GREEN);

                if (tempC > 0) {
                    channel.sendMessageEmbeds(embed.build()).queue();

                    if (tempC >= Config.getWarnThreshold()) {
                        EmbedBuilder warnEmbed = new EmbedBuilder()
                                .setTitle("⚠️ WARNING")
                                .setDescription(String.format("Pi Temp is high! (%.1f °C)", tempC))
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
