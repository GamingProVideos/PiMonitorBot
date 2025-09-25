package com.gamingprovids.utils;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    private static final Dotenv dotenv = Dotenv.load();

    private static final String TOKEN = dotenv.get("BOT_TOKEN");
    private static final long GUILD_ID = Long.parseLong(dotenv.get("GUILD_ID"));
    private static final long CHANNEL_ID = Long.parseLong(dotenv.get("CHANNEL_ID"));
    private static final String ALLOWED_ROLE_ID = dotenv.get("ALLOWED_ROLE_ID", "");

    public static final String CURRENT_VERSION = "1.0.1";
    public static final String LATEST_VERSION_URL =
            "https://raw.githubusercontent.com/GamingProVideos/PiMonitorBot/master/VERSION";

    private static int intervalMinutes = Integer.parseInt(dotenv.get("INTERVAL_MINUTES", "5"));
    private static double warnThreshold = Double.parseDouble(dotenv.get("WARN_THRESHOLD", "70.0"));

    public static String getToken() { return TOKEN; }
    public static long getGuildId() { return GUILD_ID; }
    public static long getChannelId() { return CHANNEL_ID; }
    public static String getAllowedRoleId() { return ALLOWED_ROLE_ID; }

    public static int getIntervalMinutes() { return intervalMinutes; }
    public static void setIntervalMinutes(int minutes) { intervalMinutes = minutes; }

    public static double getWarnThreshold() { return warnThreshold; }
    public static void setWarnThreshold(double threshold) { warnThreshold = threshold; }
}
