package lol.ventura.misc.font;

import com.mojang.blaze3d.systems.RenderSystem;
import lol.ventura.features.modules.render.Interface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.text.OrderedText;
import org.joml.Matrix4f;

import java.awt.*;

public class MinecraftFontRenderer implements IFontRenderer {
    @Override
    public final void drawString(String text, float x, float y, float size, Color c)
    {
        drawString(text,x,y,size,c,null);
    }

    @Override
    public void drawStringGradient(String text, float x, float y, float size, DrawContext context) {
        drawString(text,x,y,size,new Color(Interface.getTheme().forOffset(SDFRenderer.customGradientOffset)),context);
    }

    @Override
    public void drawCharRotated(char c, float x, float y, float rotate, Color color, float size, DrawContext context) {

    }

    @Override
    public void drawString(String text, float x, float y, float size, Color c, DrawContext context) {
        context.drawText(MinecraftClient.getInstance().textRenderer,text, (int) x, (int) y,c.getRGB(),false);
    }

    @Override
    public void drawStringOrdered(OrderedText text, float x, float y, float size, Color c) {
       // context.drawText(MinecraftClient.getInstance().textRenderer,text, (int) x, (int) y,c.getRGB(),false);
    }

    @Override
    public float getWidth(String text, float size) {
        return MinecraftClient.getInstance().textRenderer.getWidth(text);
    }

    @Override
    public float getWidthOrdered(OrderedText text, float size) {
        return MinecraftClient.getInstance().textRenderer.getWidth(text);
    }

    @Override
    public float getHeight(float size) {
        return MinecraftClient.getInstance().textRenderer.fontHeight;
    }
}
