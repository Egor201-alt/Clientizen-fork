package com.denizenscript.clientizen.util;

import net.minecraft.client.Minecraft;

public class FpsMonitor {

    public static int minFps = Integer.MAX_VALUE;
    public static int maxFps = 0;
    
    private static long totalFpsSum = 0;
    private static long samples = 0;

    public static void update() {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;

        int current = client.getCurrentFps();

        if (current <= 0) return;

        if (current < minFps) {
            minFps = current;
        }
        if (current > maxFps) {
            maxFps = current;
        }

        totalFpsSum += current;
        samples++;
    }

    public static int getAverage() {
        if (samples == 0) return Minecraft.getInstance().getCurrentFps();
        return (int) (totalFpsSum / samples);
    }
    
    public static void reset() {
        minFps = Integer.MAX_VALUE;
        maxFps = 0;
        totalFpsSum = 0;
        samples = 0;
    }
}
