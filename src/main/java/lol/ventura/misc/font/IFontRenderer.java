package lol.ventura.misc.font;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;

import java.awt.*;

public interface IFontRenderer {
     void drawString(String text, float x, float y, float size, Color c, DrawContext context);
     void drawStringOrdered(OrderedText text, float x, float y, float size, Color c);
     void drawString(String text, float x, float y, float size, Color c);
     void drawStringGradient(String text, float x, float y, float size, DrawContext context);
     void drawCharRotated(char c, float x, float y, float rotate, Color color, float size, DrawContext context);
     float getWidth(String text, float size);
     float getWidthOrdered(OrderedText text, float size);
     float getHeight(float size);
}
