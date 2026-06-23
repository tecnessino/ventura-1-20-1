package lol.ventura.features.ui.renderers;

import lol.ventura.features.modules.render.Interface;
import lol.ventura.features.screen.NewClickUI;
import lol.ventura.foundation.ui.ISimpleEffectRenderer;
import lol.ventura.foundation.ui.SimpleEffectBuilder;
import lol.ventura.misc.render.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Vector2f;

import java.awt.*;

public class EmissiveRenderer implements ISimpleEffectRenderer {
    @Override
    public void drawBackground(DrawContext context, Vector2f position, float width, float height, int alpha) {
    //    RenderUtil.drawRoundedRect(position.getX(),position.getY(),width,16,4, new Color(27, 25, 36));

        float thickness = 1;

        RenderUtil.drawOutlineRoundedRect(position.getX(), position.getY(), width,height,4,NewClickUI.interpolate(Interface.getTheme().getMainColor(), new Color(0,0,0,0), 0.5f), 0.5f);
        RenderUtil.drawBlurRoundedRect(context.getMatrices(), position.getX(),position.getY(),width,height,4);

        RenderUtil.drawGradientRoundedRect(position.getX(), position.getY(), width, height,4 ,NewClickUI.interpolate(Interface.getTheme().getMainColor(), new Color(0,0,0,0), 0.5f), new Color(0,0,0, 0));

       // RenderUtil.drawRoundedRect(position.getX(),position.getY(),width,height,4,new Color(29, 34, 31, 160));
    }

    @Override
    public float splitter(DrawContext context, float offset, float y, SimpleEffectBuilder.MiscInfo info) {
        float thick = 1;

        //RenderUtil.drawRoundedRect(offset,y+8-2,4,4,1,new Color(255,255,255,60));
       // offset += 4;
      // offset += info.margin();


        RenderUtil.drawRoundedRect(offset,y+2,2,12,1,new Color(255,255,255,30));



        //RenderUtil.drawRoundedRect(offset,y+8-3,6,6,2,new Color(39,40,39));
        //RenderUtil.drawRoundedRect(offset+thick,y+8-3+thick,6-thick-thick,6-thick-thick,1,new Color(23, 22, 22));
        offset += 2;
        offset += info.margin();

        return offset;
    }

    @Override
    public float splitterOffset(SimpleEffectBuilder.MiscInfo info) {
        return 2 + info.margin();
    }
}
