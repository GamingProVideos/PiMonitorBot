package com.gamingprovids.utils;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {
    private static final Dotenv dotenv = Dotenv.load();

    private static final String TOKEN = dotenv.get("BOT_TOKEN");
    private static final long GUILD_ID = parseLong(dotenv.get("GUILD_ID"), 0);
    private static final long CHANNEL_ID = parseLong(dotenv.get("CHANNEL_ID"), 0);
    private static final String ALLOWED_ROLE_ID = dotenv.get("ALLOWED_ROLE_ID", "");

    public static final String CURRENT_VERSION = "1.0.3";
    public static final String LATEST_VERSION_URL =
            "https://raw.githubusercontent.com/GamingProVideos/PiMonitorBot/master/VERSION";

    private static int intervalMinutes = parseInt(dotenv.get("INTERVAL_MINUTES"), 5);
    private static double warnThreshold = parseDouble(dotenv.get("WARN_THRESHOLD"), 70.0);

    // Utility parsing with defaults
    private static int parseInt(String value, int defaultVal) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            System.err.println("⚠️ Invalid integer in config: " + value + ", using default: " + defaultVal);
            return defaultVal;
        }
    }

    private static double parseDouble(String value, double defaultVal) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            System.err.println("⚠️ Invalid double in config: " + value + ", using default: " + defaultVal);
            return defaultVal;
        }
    }

    private static long parseLong(String value, long defaultVal) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            System.err.println("⚠️ Invalid long in config: " + value + ", using default: " + defaultVal);
            return defaultVal;
        }
    }

    // Getters
    public static String getToken() { return TOKEN; }
    public static long getGuildId() { return GUILD_ID; }
    public static long getChannelId() { return CHANNEL_ID; }
    public static String getAllowedRoleId() { return ALLOWED_ROLE_ID; }

    public static int getIntervalMinutes() { return intervalMinutes; }
    public static void setIntervalMinutes(int minutes) { intervalMinutes = minutes; }

    public static double getWarnThreshold() { return warnThreshold; }
    public static void setWarnThreshold(double threshold) { warnThreshold = threshold; }
}
