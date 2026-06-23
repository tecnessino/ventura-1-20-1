package lol.ventura.features.ui;

import lol.ventura.features.modules.render.Interface;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleRepository;
import lol.ventura.foundation.property.Property;
import lol.ventura.foundation.themes.ColorUtil;
import lol.ventura.foundation.ui.Effect;
import lol.ventura.misc.font.IFontRenderer;
import lol.ventura.misc.font.SDFRenderer;
import lol.ventura.misc.render.RenderUtil;
import lol.ventura.misc.render.ScaledResolution;
import lol.ventura.misc.render.StencilUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Vector2f;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ArrayList extends Effect {
    @Override
    public String getName() {
        return "ArrayList";
    }

    @Override
    public List<Property> getProperties() {
        return List.of();
    }

    @Override
    public Vector2f draw(DrawContext context, int x, int y) {
        List<Module> module = null;

        IFontRenderer font = Interface.getFont();

        module = new java.util.ArrayList<>(ModuleRepository.getInstance().getModules().stream().sorted(Comparator.comparingDouble((m) -> {
            return font.getWidth(m.getDescriptor().name(), 8);
        })).toList());
        Collections.reverse(module);
        float longest = font.getWidth(module.get(0).getDescriptor().name(), 8);

        boolean left = x <= ScaledResolution.getWidth()/2;


        //Wont work performant
        /*StencilUtil.bindWrite();
        RenderUtil.drawRoundedRectDepthed(context.getMatrices(), 100,100,100,100,6,Color.white);
        StencilUtil.bindReadExclude();
        RenderUtil.draw
        StencilUtil.disable();*/

        float offset = y;
        long colorOffset = 0;
        for(Module m : module)
        {


            float moduleX = (float) m.getXAnimation().getValue();
            float moduleY = (float) m.getYAnimation().getValue();

            float difference = ScaledResolution.getWidth() - x - longest;
            float targetX = left ? x : ScaledResolution.getWidth() - font.getWidth(m.getDescriptor().name(), 8) - difference;

            m.getXAnimation().update();
            m.getXAnimation().animate(m.isEnabled() ? targetX : (left ? -100 : ScaledResolution.getWidth()+5), 0.1f);

            m.getYAnimation().update();
            m.getYAnimation().animate(offset, 0.1f);

            RenderUtil.drawRect(
                    context.getMatrices(),
                    moduleX-4,
                    moduleY-2,
                    font.getWidth(m.getDescriptor().name(), 8)+8,
                    Interface.getFontFixedHalfHeight()*2+4,
                    new Color(0,0,0,32)
            );

            RenderUtil.drawRect(
                    context.getMatrices(),
                    moduleX + (left ? -5 : font.getWidth(m.getDescriptor().name(), 8)+3),
                    moduleY-2,
                    1,
                    Interface.getFontFixedHalfHeight()*2+4,
                    new Color(Interface.getTheme().forOffset(colorOffset))
                    );

            long finalColorOffset = colorOffset;
            RenderUtil.drawPostBloom((ctx) -> {
                RenderUtil.drawRect(
                        ctx,
                        moduleX + (left ? -5 : font.getWidth(m.getDescriptor().name(), 8)+3),
                        moduleY-2,
                        1,
                        Interface.getFontFixedHalfHeight()*2+4,
                        new Color(Interface.getTheme().forOffset(finalColorOffset))
                );
            });

            RenderUtil.drawBlurRoundedRect(
                    context.getMatrices(),
                    moduleX-4,
                    moduleY-2,
                    font.getWidth(m.getDescriptor().name(), 8)+8,
                    Interface.getFontFixedHalfHeight()*2+4,
                    0
            );
            SDFRenderer.customGradientOffset = colorOffset;
            font.drawStringGradient(m.getDescriptor().name(), moduleX, moduleY,8, context);
            if(m.isEnabled())
            {
                offset += Interface.getFontFixedHalfHeight()*2+4;
                colorOffset+=300;
            }
        }
        return new Vector2f(longest,offset - y);
    }
}
