package lol.ventura.features.ui.renderers;

import lol.ventura.foundation.ui.ISimpleEffectRenderer;
import lol.ventura.foundation.ui.SimpleEffectBuilder;
import lol.ventura.misc.render.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Vector2f;

import java.awt.*;

public class BlurRenderer implements ISimpleEffectRenderer {
    @Override
    public void drawBackground(DrawContext context, Vector2f position, float width, float height, int alpha) {
        float thickness = 1;

        RenderUtil.drawOutlineRoundedRect(position.getX(), position.getY(), width,height,4,new Color(29, 34, 31, 120), 0.5f);
        RenderUtil.drawBlurRoundedRect(context.getMatrices(), position.getX(),position.getY(),width,height,4);

        RenderUtil.drawRoundedRect(position.getX(),position.getY(),width,height,4,new Color(29, 34, 31, 160));
    }

    @Override
    public float splitter(DrawContext context, float offset, float y, SimpleEffectBuilder.MiscInfo info) {
        float thick = 1;
        RenderUtil.drawOutlineRoundedRect(offset,y+8-3,6,6,2,new Color(255,255,255, 60),1f);
        offset += 6;
        offset += info.margin();

        return offset;
    }

    @Override
    public float splitterOffset(SimpleEffectBuilder.MiscInfo info) {
        return 6 + info.margin();
    }
}
