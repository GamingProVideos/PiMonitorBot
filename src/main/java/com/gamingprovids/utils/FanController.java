package com.gamingprovids.utils;

public class FanController {

    private static boolean autoMode = false;
    private static int manualPercent = 0;
    private static Thread fanThread;

    /** Enable auto fan mode */
    public static void enableAuto() {
        autoMode = true;
        startFanThread();
    }

    /** Disable auto fan mode */
    public static void disableAuto() {
        autoMode = false;
        stopFanThread();
    }

    /** Set manual fan speed (0-100%) */
    public static void setManualPercent(int percent) {
        manualPercent = Math.max(0, Math.min(percent, 100));
        disableAuto();
        setFanSpeed(manualPercent);
    }

    /** Returns true if auto mode is active */
    public static boolean isAuto() {
        return autoMode;
    }

    /** Returns the current target fan speed in percent */
    public static int getCurrentPercent() {
        return autoMode ? calculateAutoPercent() : manualPercent;
    }

    /** Thread loop for auto fan control */
    private static void startFanThread() {
        if (fanThread != null && fanThread.isAlive()) return;

        fanThread = new Thread(() -> {
            while (autoMode) {
                try {
                    int percent = calculateAutoPercent();
                    setFanSpeed(percent);
                    Thread.sleep(2000); // adjust every 2 seconds
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        fanThread.setDaemon(true);
        fanThread.start();
    }

    private static void stopFanThread() {
        if (fanThread != null) fanThread.interrupt();
    }

    /** Auto fan curve: CPU temp -> fan % */
    private static int calculateAutoPercent() {
        double cpuTemp = PiUtils.getCpuTemperature();
        if (cpuTemp <= 40) return 30;
        if (cpuTemp >= 70) return 100;
        return (int) ((cpuTemp - 40) / 30.0 * 70 + 30); // linear ramp 30-100%
    }

    /** Actual fan write to the Pi */
    public static void setFanSpeed(int percent) {
        try {
            int maxState = 10; // default max_state
            // read max_state from system if possible
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.FileReader("/sys/class/thermal/cooling_device0/max_state"))) {
                maxState = Integer.parseInt(br.readLine().trim());
            } catch (Exception ignored) {}

            int state = (int) Math.round(percent / 100.0 * maxState);

            Process proc = new ProcessBuilder("sudo", "tee", "/sys/class/thermal/cooling_device0/cur_state")
                    .redirectErrorStream(true).start();
            proc.getOutputStream().write(String.valueOf(state).getBytes());
            proc.getOutputStream().flush();
            proc.getOutputStream().close();
            proc.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
