package com.mzcloud.djt.module_face.camera.utilclass;

public class MathUtils {
    private MathUtils() {
    }

    public static int round(float d) {
        return (int)(d + (d < 0.0F ? -0.5F : 0.5F));
    }

    public static float distance(float aX, float aY, float bX, float bY) {
        float xDiff = aX - bX;
        float yDiff = aY - bY;
        return (float)Math.sqrt((double)(xDiff * xDiff + yDiff * yDiff));
    }

    public static float distance(int aX, int aY, int bX, int bY) {
        int xDiff = aX - bX;
        int yDiff = aY - bY;
        return (float)Math.sqrt((double)(xDiff * xDiff + yDiff * yDiff));
    }

    public static int sum(int[] array) {
        int count = 0;
        int[] var5 = array;
        int var4 = array.length;

        for(int var3 = 0; var3 < var4; ++var3) {
            int a = var5[var3];
            count += a;
        }

        return count;
    }
}
