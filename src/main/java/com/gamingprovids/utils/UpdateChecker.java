package com.gamingprovids.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateChecker {
    private final JDA jda;
    private static Message lastUpdateMessage = null;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UpdateChecker(JDA jda) {
        this.jda = jda;
    }

    public void checkForUpdates() {
        executor.submit(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(Config.LATEST_VERSION_URL).openConnection();
                conn.setRequestProperty("User-Agent", "PiMonitorBot");

                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String latestVersion = in.readLine();
                    if (latestVersion == null) return;

                    latestVersion = latestVersion.trim();
                    if (!Config.CURRENT_VERSION.equalsIgnoreCase(latestVersion)) {
                        TextChannel channel = jda.getTextChannelById(Config.getChannelId());
                        if (channel == null) return;

                        EmbedBuilder updateEmbed = new EmbedBuilder()
                                .setTitle("🔄 Update Available")
                                .setDescription("Running version **" + Config.CURRENT_VERSION + "**\n" +
                                        "Latest version is **" + latestVersion + "**\n\n" +
                                        "Update here: [GitHub Repo](https://github.com/GamingProVideos/PiMonitorBot)")
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
        });
    }
}
