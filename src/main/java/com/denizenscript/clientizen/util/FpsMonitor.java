package com.denizenscript.clientizen.util;

import net.minecraft.client.Minecraft;
import java.lang.reflect.Field;

public class FpsMonitor {

    public static int minFps = Integer.MAX_VALUE;
    public static int maxFps = 0;
    
    private static long totalFpsSum = 0;
    private static long samples = 0;

    // Поле для хранения доступа к приватной переменной fps
    private static Field fpsField;

    static {
        try {
            try {
                fpsField = Minecraft.class.getDeclaredField("fps");
            } catch (NoSuchFieldException e) {
                fpsField = Minecraft.class.getDeclaredField("field_1738");
            }
            fpsField.setAccessible(true);
        } catch (Exception e) {
            System.err.println("[Clientizen-Fork] Failed to access FPS field via reflection:");
            e.printStackTrace();
        }
    }

    public static int getCurrentGameFps() {
        try {
            if (fpsField != null) {
                Minecraft client = Minecraft.getInstance();
                if (client != null) {
                    return fpsField.getInt(client);
                }
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public static void update() {
        int current = getCurrentGameFps();

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
        if (samples == 0) return getCurrentGameFps();
        return (int) (totalFpsSum / samples);
    }
    
    public static void reset() {
        minFps = Integer.MAX_VALUE;
        maxFps = 0;
        totalFpsSum = 0;
        samples = 0;
    }
}
