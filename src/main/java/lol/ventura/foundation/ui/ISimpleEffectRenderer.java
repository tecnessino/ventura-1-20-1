package lol.ventura.foundation.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Vector2f;

public interface ISimpleEffectRenderer {
    void drawBackground(DrawContext context, Vector2f pos, float width, float height, int alpha);

    default void drawBackground(DrawContext context, Vector2f pos, float width, float height)
    {
        drawBackground(context, pos,width,height,255);
    }

    //COMPONENTS
    float splitter(DrawContext context, float x, float y, SimpleEffectBuilder.MiscInfo info);
    float splitterOffset(SimpleEffectBuilder.MiscInfo info);
}
