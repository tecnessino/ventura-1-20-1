package lol.ventura.features.ui.renderers;

import lol.ventura.foundation.ui.ISimpleEffectRenderer;
import lol.ventura.foundation.ui.SimpleEffectBuilder;
import lol.ventura.misc.render.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Vector2f;

import java.awt.*;

public class ClassicRenderer implements ISimpleEffectRenderer {
    @Override
    public void drawBackground(DrawContext context, Vector2f position, float width, float height, int alpha) {
        RenderUtil.drawRoundedRect(position.getX(),position.getY(),width,height,4, new Color(27, 25, 36, alpha));
        RenderUtil.drawPreBloom((matrix) -> {
            RenderUtil.drawRoundedRect(position.getX(),position.getY(),width,height,4, new Color(27, 25, 36, alpha));
        });
    }

    @Override
    public float splitter(DrawContext context, float offset, float y, SimpleEffectBuilder.MiscInfo info) {
        RenderUtil.drawRoundedRect(offset,y+8-2,4,4,1,new Color(75, 72, 89));
        offset += 5;
        offset += info.margin();

        return offset;
    }

    @Override
    public float splitterOffset(SimpleEffectBuilder.MiscInfo info) {
        return 5 + info.margin();
    }
}
