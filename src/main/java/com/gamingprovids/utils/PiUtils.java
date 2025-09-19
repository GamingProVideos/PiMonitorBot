package com.gamingprovids.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PiUtils {

    private PiUtils() {
        // private constructor to prevent instantiation
    }

    /**
     * Reads the Raspberry Pi CPU temperature in °C.
     *
     * @return CPU temperature in Celsius, or -1.0 if unable to read
     */
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

    /**
     * Reads the Raspberry Pi GPU temperature in °C using vcgencmd.
     *
     * @return GPU temperature in Celsius, or -1.0 if unable to read
     */
    public static double getGpuTemperature() {
        try {
            Process process = new ProcessBuilder("vcgencmd", "measure_temp").start();
            try (BufferedReader br = new BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
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

    /**
     * Reads the Raspberry Pi fan speed as a percentage of max.
     *
     * @return fan speed percentage, or -1.0 if unavailable
     */
    public static double getFanPercentage() {
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
