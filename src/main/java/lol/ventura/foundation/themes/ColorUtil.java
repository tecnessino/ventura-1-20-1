package lol.ventura.foundation.themes;

import lol.ventura.features.modules.render.Interface;

import java.awt.*;
import java.util.Random;

public class ColorUtil {
    public static int Rainbow(int var2, float bright, float st, long index) {
        float hue = ((System.currentTimeMillis() + index) % (int)(var2 * 1000) / (float)(var2 * 1000));
        int color = Color.HSBtoRGB(hue, st, bright);
        return color;
    }

    public static int Rainbow(int var2, float bright, float st) {
        float hue = ((System.currentTimeMillis() % (int)(var2 * 1000) / (float)(var2 * 1000)));
        int color = Color.HSBtoRGB(hue, st, bright);
        return color;
    }

    public static int fadeBetween(int color1, int color2, float offset) {
        if (offset > 1)
            offset = 1 - offset % 1;

        double invert = 1 - offset;
        int r = (int) ((color1 >> 16 & 0xFF) * invert +
                (color2 >> 16 & 0xFF) * offset);
        int g = (int) ((color1 >> 8 & 0xFF) * invert +
                (color2 >> 8 & 0xFF) * offset);
        int b = (int) ((color1 & 0xFF) * invert +
                (color2 & 0xFF) * offset);
        int a = (int) ((color1 >> 24 & 0xFF) * invert +
                (color2 >> 24 & 0xFF) * offset);
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF);
    }

    public static int darker(int color, float factor) {
        int r = (int) ((color >> 16 & 0xFF) * factor);
        int g = (int) ((color >> 8 & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        int a = color >> 24 & 0xFF;

        return ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF) |
                ((a & 0xFF) << 24);
    }

    public static int Astolfo(int var2, float bright, float st, long index) {
        double v1 = Math.ceil(System.currentTimeMillis() + index + (long) (var2 * 109)) / 5;
        return Color.getHSBColor((double) ((float) ((v1 %= 360.0) / 360.0)) < 0.5 ? -((float) (v1 / 360.0)) : (float) (v1 / 360.0), st, bright).getRGB();
    }

    public static int pastel() {
        int R = (int)(Math.random()*256);
        int G = (int)(Math.random()*256);
        int B= (int)(Math.random()*256);
        Color color = new Color(R, G, B);
        Random random = new Random();
        final float hue = random.nextFloat();
        final float saturation = 0.9f;
        final float luminance = 1.0f;
        color = Color.getHSBColor(hue, saturation, luminance);

        return color.getRGB();
    }

    public static int MainColor() {
        return Interface.getTheme().getMainColor().getRGB();
    }

    public static int AccentColor() {
        return Interface.getTheme().getSecondColor().getRGB();
    }
}