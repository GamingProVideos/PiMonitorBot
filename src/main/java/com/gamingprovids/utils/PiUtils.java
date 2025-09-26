package com.gamingprovids.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PiUtils {

    private PiUtils() {}

    /** Reads CPU temperature in °C */
    public static double getCpuTemperature() {
        String path = "/sys/class/thermal/thermal_zone0/temp";
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            if (line != null) return Integer.parseInt(line) / 1000.0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1.0;
    }

    /** Reads GPU temperature in °C using vcgencmd */
    public static double getGpuTemperature() {
        try {
            Process process = new ProcessBuilder("vcgencmd", "measure_temp").start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = br.readLine(); // e.g., temp=48.2'C
                if (line != null && line.startsWith("temp=")) {
                    line = line.replace("temp=", "").replace("'C", "");
                    return Double.parseDouble(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1.0;
    }

    /** Reads current fan RPM */
    public static int getFanRpm() {
        String path = "/sys/class/hwmon/hwmon2/fan1_input";
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            if (line != null) return Integer.parseInt(line.trim());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /** Reads max fan RPM (fallback to 5000 if not available) */
    public static int getFanMaxRpm() {
        String path = "/sys/class/hwmon/hwmon2/fan1_max";
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            if (line != null) return Integer.parseInt(line.trim());
        } catch (IOException e) {
            // not all hwmon drivers expose fan1_max
        }
        return 5000;
    }

    /** Returns fan speed as a percentage of max RPM */
    public static double getFanPercentageFromRpm() {
        int rpm = getFanRpm();
        int maxRpm = getFanMaxRpm();
        if (rpm >= 0 && maxRpm > 0) {
            double percent = (rpm / (double) maxRpm) * 100.0;
            return Math.max(0, Math.min(percent, 100)); // clamp 0–100%
        }
        return -1.0;
    }

    /** Returns CPU frequency in MHz (current overclock) */
    public static double getCpuOverclock() {
        try (BufferedReader br = new BufferedReader(new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq"))) {
            String line = br.readLine();
            if (line != null) return Integer.parseInt(line.trim()) / 1000.0; // kHz → MHz
        } catch (IOException ignored) {}
        return -1;
    }

    /** Returns GPU frequency in MHz */
    public static double getGpuOverclock() {
        try {
            Process proc = new ProcessBuilder("vcgencmd", "measure_clock", "core").start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                String line = br.readLine();
                if (line != null && line.startsWith("frequency(")) {
                    String value = line.split("=")[1].trim();
                    return Double.parseDouble(value) / 1_000_000.0; // Hz → MHz
                }
            }
        } catch (IOException ignored) {}
        return -1;
    }
}
