package lol.ventura.features.ui.renderers;

import lol.ventura.foundation.ui.ISimpleEffectRenderer;
import lol.ventura.foundation.ui.SimpleEffectBuilder;
import lol.ventura.misc.render.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Vector2f;

import java.awt.*;

public class AlternativeRenderer implements ISimpleEffectRenderer {
    @Override
    public void drawBackground(DrawContext context, Vector2f position, float width, float height, int alpha) {
    //    RenderUtil.drawRoundedRect(position.getX(),position.getY(),width,16,4, new Color(27, 25, 36));

        float thickness = 1;

        RenderUtil.drawRoundedRect(position.getX(),position.getY(),width,height,4,new Color(29, 34, 31, alpha));
        RenderUtil.drawRoundedRect(position.getX()+thickness,position.getY()+thickness,width-thickness-thickness,height-thickness-thickness,4,new Color(23, 22, 22, alpha));

        RenderUtil.drawPreBloom((matrix) -> {
            RenderUtil.drawRoundedRect(position.getX(),position.getY(),width,height,4,new Color(29, 34, 31, alpha));
        });
    }

    @Override
    public float splitter(DrawContext context, float offset, float y, SimpleEffectBuilder.MiscInfo info) {
        float thick = 1;
        RenderUtil.drawRoundedRect(offset,y+8-3,6,6,2,new Color(39,40,39));
        RenderUtil.drawRoundedRect(offset+thick,y+8-3+thick,6-thick-thick,6-thick-thick,1,new Color(23, 22, 22));
        offset += 6;
        offset += info.margin();

        return offset;
    }

    @Override
    public float splitterOffset(SimpleEffectBuilder.MiscInfo info) {
        return 6 + info.margin();
    }
}
