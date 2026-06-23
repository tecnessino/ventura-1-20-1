package lol.ventura.misc.render;

import net.minecraft.client.MinecraftClient;

public final class ScaledResolution {
    public static float getWidth()
    {
        return (float) (MinecraftClient.getInstance().getWindow().getWidth() / MinecraftClient.getInstance().getWindow().getScaleFactor());
    }

    public static float getHeight()
    {
        return (float) (MinecraftClient.getInstance().getWindow().getHeight() / MinecraftClient.getInstance().getWindow().getScaleFactor());
    }
}
