package lol.ventura.foundation.ui;

import lol.ventura.features.modules.render.Interface;
import lol.ventura.misc.font.IFontRenderer;
import lol.ventura.misc.font.MinecraftFontRenderer;
import lol.ventura.misc.render.RenderUtil;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.Vector2f;

import java.awt.*;
import java.util.ArrayList;

public class SimpleEffectBuilder {

    public static interface IComponentDrawer {
        public float draw(DrawContext context, float inOffset, float y, MiscInfo info);
    }

    public static record MiscInfo(float margin) {}

    @Getter
    private float width = 0;
    private float margin = 0;
    private ArrayList<IComponentDrawer> drawables = new ArrayList<>();
    private ISimpleEffectRenderer renderer;
    private MiscInfo info;

    public SimpleEffectBuilder(float margin, ISimpleEffectRenderer renderer)
    {
        this.margin = margin;
        this.renderer = renderer;

        width += margin+4+4;
        this.info = new MiscInfo(margin);
    }

    public SimpleEffectBuilder splitter()
    {
        drawables.add(renderer::splitter);
        width += renderer.splitterOffset(info);
        return this;
    }

    public SimpleEffectBuilder text(IFontRenderer font, String text)
    {
        return text(font, text, Color.white);
    }

    public SimpleEffectBuilder text(IFontRenderer font, String text, Color c)
    {
        drawables.add((ctx, offset,y, info) -> {
            float halfHeight = Interface.getFontFixedHalfHeight();

            if(font instanceof MinecraftFontRenderer)
            {
                halfHeight = font.getHeight(8)-5.501f;
            }

            font.drawString(text,offset,y+8-halfHeight,8,c,ctx);
            offset += font.getWidth(text, 8);
            offset += margin;
            return offset;
        });

        width += font.getWidth(text, 8);
        width += margin;
        return this;
    }

    public SimpleEffectBuilder icon(IFontRenderer font, String text, int size,final float fixedHalfHeight) {
        drawables.add((ctx, offset,y, info) -> {
            font.drawString(text,offset,y+8-fixedHalfHeight,size,Color.white,ctx);
            offset += font.getWidth(text, size);
            offset += margin;
            return offset;
        });

        width += font.getWidth(text, size);
        width += margin;
        return this;
    }

    public SimpleEffectBuilder sprite(Sprite sprite)
    {
        final int size = 9;
        drawables.add((ctx,offset,y,info) -> {
            ctx.drawSprite((int) offset, (int) (y + 8 - (size * 0.5f)),0,size, size, sprite);
            offset += size;
            offset += margin;
            return offset;
        });
        width += size;
        width += margin;

        return this;
    }

    public SimpleEffectBuilder textBloomGradient(IFontRenderer font, String text)
    {
        drawables.add((ctx, offset,y, info) -> {
            final float halfHeight = font instanceof MinecraftFontRenderer ? font.getHeight(8)-5.501f : Interface.getFontFixedHalfHeight();

            font.drawStringGradient(text,offset,y+8-halfHeight,8,ctx);

            float finalOffset = offset;
            RenderUtil.drawPostBloom((ctx2) -> {
                font.drawStringGradient(text, finalOffset,y+8-halfHeight,8,ctx);
            });
            offset += font.getWidth(text, 8);
            offset += margin;
            return offset;
        });

        width += font.getWidth(text, 8);
        width += margin;
        return this;
    }

    public SimpleEffectBuilder textBloom(IFontRenderer font, String text, Color c)
    {
        drawables.add((ctx, offset,y, info) -> {
            final float halfHeight = font instanceof MinecraftFontRenderer ? font.getHeight(8)-5.501f : Interface.getFontFixedHalfHeight();

            font.drawString(text,offset,y+8-halfHeight,8,c,ctx);

            float finalOffset = offset;
            RenderUtil.drawPostBloom((ctx2) -> {
                font.drawString(text, finalOffset,y+8-halfHeight,8,c,ctx);
            });
            offset += font.getWidth(text, 8);
            offset += margin;
            return offset;
        });

        width += font.getWidth(text, 8);
        width += margin;
        return this;
    }

    public void draw(DrawContext context, Vector2f position)
    {
        renderer.drawBackground(context, position, width, 16);
        float offset = position.getX()+margin+4;
        for(IComponentDrawer drawer : drawables)
        {
            offset = drawer.draw(context, offset , position.getY(), info);
        }

    }

}
