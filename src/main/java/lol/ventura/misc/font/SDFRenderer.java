package lol.ventura.misc.font;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import lol.ventura.VenturaClient;
import lol.ventura.features.modules.render.Interface;
import lol.ventura.features.shaders.GradientSDFShader;
import lol.ventura.features.shaders.SDFShader;
import lol.ventura.misc.math.BoundsEx;
import lol.ventura.misc.render.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;
import org.joml.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public final class SDFRenderer implements IFontRenderer {

    private final int atlasId;
    private final HashMap<Integer, FontGlyph> glyphs = new HashMap<>();
    private final Metrics metrics;
    private final float atlasWidth, atlasHeight;

    private final static String FORMATTING_PALETTE = "0123456789abcdefklmnor";
    private final static int[][] FORMATTING_COLOR_PALETTE = new int[32][3];

    static {
        for (int i = 0; i < 32; ++i) {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i & 1) * 170 + j;

            if (i == 6) {
                k += 85;
            }

            if (i >= 16) {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }

            FORMATTING_COLOR_PALETTE[i][0] = k;
            FORMATTING_COLOR_PALETTE[i][1] = l;
            FORMATTING_COLOR_PALETTE[i][2] = i1;
        }
    }


    private final BoundsEx getBoundsFromJson(JsonObject object)
    {
        return new BoundsEx(
                object.get("left").getAsFloat(),
                object.get("right").getAsFloat(),
                object.get("bottom").getAsFloat(),
                object.get("top").getAsFloat()
        );
    }

    public record Metrics(
            float emSize,
            float lineHeight,
            float ascender,
            float descender,
            float underlineY,
            float underlineThickness)
    { }

    public record FontGlyph(
            int character,
            float advance,
            BoundsEx planeBounds,
            BoundsEx atlasBounds)
    { }

    /*float fsScale = (1.0f / (metrics.ascender - metrics.descender)) * size;

            if(character == ' ')
                x += fsScale * 0.25f;

            FontGlyph glyph = glyphs.get(character);
            if(glyph == null)
                continue;

            /*Vector2f quadMin = new Vector2f(glyph.planeBounds.getLeft(), glyph.planeBounds.getTop());
            Vector2f quadMax = new Vector2f(quadMin).add(glyph.planeBounds.getRight(), =- );



            quadMin.mul(fsScale);
            quadMax.mul(fsScale);
            quadMin.add(x,y);
            quadMax.add(x,y);

            Vector2f atlasMin = new Vector2f(0,0);
            Vector2f atlasMax = new Vector2f(1,1);

            bufferBuilder.vertex(  quadMin.x, quadMax.y, 0.0F).texture(atlasMin.x,atlasMax.y).next();
            bufferBuilder.vertex( quadMax.x, quadMax.y, 0.0F).texture(atlasMax.x, atlasMax.y).next();
            bufferBuilder.vertex( quadMax.x, quadMin.y, 0.0F).texture(atlasMax.x, atlasMin.y).next();
            bufferBuilder.vertex( quadMin.x, quadMin.y, 0.0F).texture(atlasMin.x, atlasMin.y).next();

    float width = glyph.planeBounds.getRight() - glyph.planeBounds.getLeft();
    float height = glyph.planeBounds.getTop() - glyph.planeBounds.getBottom();

    width *= fsScale;
    height *= fsScale;

    float aLeft = glyph.atlasBounds.getLeft() / atlasWidth;
    float aRight = glyph.atlasBounds.getRight() / atlasWidth;
    float aTop = glyph.atlasBounds.getTop() / atlasHeight;
    float aBottom = glyph.atlasBounds.getBottom() / atlasHeight;

    Vector2f pos = new Vector2f(x,y);

    float baseAdj = pos.y - height + (metrics.lineHeight * fsScale);
    float ascenderAdj = Math.abs(glyph.planeBounds.getBottom()) * (metrics.ascender * fsScale + 1);
    pos.y =baseAdj + ascenderAdj;

            bufferBuilder.vertex(pos.x, pos.y + height, 0.0F).texture(aLeft, aBottom).next();
            bufferBuilder.vertex(pos.x + width, pos.y + height, 0.0F).texture(aRight, aBottom).next();
            bufferBuilder.vertex(pos.x + width, pos.y, 0.0F).texture(aRight, aTop).next();
            bufferBuilder.vertex(pos.x, pos.y, 0.0F).texture(aLeft, aTop).next();


    //shader.draw(atlasId, quadMin, quadMax, atlasMin, atlasMax   , Color.green);

    x += fsScale * glyph.advance;
    // System.out.print(glyph.character);*/

    public SDFRenderer(final JsonObject meta, final String name, final NativeImageBackedTexture image)
    {
        this.atlasId = image.getGlId();
        this.metrics = new Gson().fromJson(meta.get("metrics"), Metrics.class);
        this.atlasWidth = image.getImage().getWidth();
        this.atlasHeight = image.getImage().getHeight();
        image.setFilter(true, false);
        //image.upload();

        MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().getTextureManager().registerTexture(Identifier.of("ventura","atlas_" + name), image));

        for(JsonElement bully : meta.getAsJsonArray("glyphs").asList())
        {
            JsonObject object = bully.getAsJsonObject();

            if(!object.has("planeBounds"))
                continue;

            int c = 0;
            if(object.has("index"))
            {
                c = object.get("index").getAsInt();
            } else {
                c = object.get("unicode").getAsInt();
            }

            FontGlyph glyph = new FontGlyph(
                    c,
                    object.get("advance").getAsFloat(),
                    getBoundsFromJson(object.getAsJsonObject("planeBounds")),
                    getBoundsFromJson(object.getAsJsonObject("atlasBounds"))
            );

            glyphs.put(glyph.character, glyph);
        }

        VenturaClient.getLogger().info("Loaded {} characters", glyphs.size());
    }

    public final void drawStringOrdered(OrderedText text, float x, float y, float size, Color c)
    {
        SDFShader shader = RenderUtil.getSdfShader();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();


        float fsScale = (1.0f / (metrics.ascender - metrics.descender)) * size;

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        AtomicReference<Float> textX = new AtomicReference<>(x);
        text.accept((index, style, codePoint) -> {
            if (codePoint > 256) {
                return true;
            }

            Color ce = null;

            if (style.getColor() == null) {
                ce = c;
            } else {
                ce = new Color(style.getColor().getRgb());
            }

            if((char)codePoint == ' ')
                textX.set(textX.get() + (fsScale * 0.25f));

            FontGlyph glyph = glyphs.get((int)codePoint);
            if(glyph == null)
                return true;

            final float scaledSize = size,
                    ascender = metrics.ascender * scaledSize,
                    left = glyph.planeBounds.getLeft() * scaledSize,
                    right = glyph.planeBounds.getRight() * scaledSize,
                    top = glyph.planeBounds.getTop() * scaledSize,
                    bottom = glyph.planeBounds.getBottom() * scaledSize,
                    atlasW = atlasWidth,
                    atlasH = atlasHeight;

            float x0 = textX.get() + left,
                    x1 = textX.get() + right,
                    y0 = y + (ascender - top),
                    y1 = y + (ascender - bottom),
                    u0 = glyph.atlasBounds.getLeft() / atlasW,
                    u1 = glyph.atlasBounds.getRight() / atlasW,
                    v0 = glyph.atlasBounds.getTop() / atlasH,
                    v1 = glyph.atlasBounds.getBottom() / atlasH;

            y0 += metrics.underlineY * size;
            y1 += metrics.underlineY * size;

            bufferBuilder.vertex( x0, y0, 0);
            bufferBuilder.texture(u0, v0).color(ce.getRed(), ce.getGreen(), ce.getBlue(), ce.getAlpha()).next();
            bufferBuilder.vertex( x0, y1, 0);
            bufferBuilder.texture(u0, v1).color(ce.getRed(), ce.getGreen(), ce.getBlue(), ce.getAlpha()).next();
            bufferBuilder.vertex( x1 , y1, 0);
            bufferBuilder.texture(u1, v1).color(ce.getRed(), ce.getGreen(), ce.getBlue(), ce.getAlpha()).next();
            bufferBuilder.vertex( x1, y0, 0);
            bufferBuilder.texture(u1, v0).color(ce.getRed(), ce.getGreen(), ce.getBlue(), ce.getAlpha()).next();

            textX.set(textX.get() + (glyph.advance * size));
            return true;
        });

        shader.bind(atlasId, c, new Vector2f(0,0), 0);
        BufferRenderer.draw(bufferBuilder.end());
        shader.unbind();

        RenderSystem.disableBlend();
    }

    public final void drawStrings(Multimap<String, Vector2f> strings, float size, Color c)
    {
        SDFShader shader = RenderUtil.getSdfShader();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();


        float fsScale = (1.0f / (metrics.ascender - metrics.descender)) * size;

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        for(var entry : strings.entries())
        {
            float x = entry.getValue().x;
            float y = entry.getValue().y;

            for(final char character : entry.getKey().toCharArray())
            {
                if(character == ' ')
                    x += fsScale * 0.25f;

                FontGlyph glyph = glyphs.get((int)character);
                if(glyph == null)
                    continue;

                final float scaledSize = size,
                        ascender = metrics.ascender * scaledSize,
                        left = glyph.planeBounds.getLeft() * scaledSize,
                        right = glyph.planeBounds.getRight() * scaledSize,
                        top = glyph.planeBounds.getTop() * scaledSize,
                        bottom = glyph.planeBounds.getBottom() * scaledSize,
                        atlasW = atlasWidth,
                        atlasH = atlasHeight;

                float x0 = x + left,
                        x1 = x + right,
                        y0 = y + (ascender - top),
                        y1 = y + (ascender - bottom),
                        u0 = glyph.atlasBounds.getLeft() / atlasW,
                        u1 = glyph.atlasBounds.getRight() / atlasW,
                        v0 = glyph.atlasBounds.getTop() / atlasH,
                        v1 = glyph.atlasBounds.getBottom() / atlasH;

                y0 += metrics.underlineY * size;
                y1 += metrics.underlineY * size;

                bufferBuilder.vertex( x0, y0, 0);
                bufferBuilder.texture(u0, v0).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).next();
                bufferBuilder.vertex( x0, y1, 0);
                bufferBuilder.texture(u0, v1).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).next();
                bufferBuilder.vertex( x1 , y1, 0);
                bufferBuilder.texture(u1, v1).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).next();
                bufferBuilder.vertex( x1, y0, 0);
                bufferBuilder.texture(u1, v0).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).next();

                x += glyph.advance * size;
            }
        }

        shader.bind(atlasId, c, new Vector2f(0,0), 0);
        BufferRenderer.draw(bufferBuilder.end());
        shader.unbind();

        RenderSystem.disableBlend();
    }

    public static long customGradientOffset = 0;
    @Override
    public final void drawStringGradient(String text, float x, float y, float size, DrawContext context)
    {
        GradientSDFShader shader = RenderUtil.getGradientSDFShader();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        long colorOffset = 0;
        for(final char character : text.toCharArray())
        {
            float fsScale = (1.0f / (metrics.ascender - metrics.descender)) * size;
            Color c = new Color(Interface.getTheme().forOffset(colorOffset + customGradientOffset));
            if(character == ' ')
                x += fsScale * 0.25f;

            FontGlyph glyph = glyphs.get((int)character);
            if(glyph == null)
                continue;

            final float scaledSize = size,
                    ascender = metrics.ascender * scaledSize,
                    left = glyph.planeBounds.getLeft() * scaledSize,
                    right = glyph.planeBounds.getRight() * scaledSize,
                    top = glyph.planeBounds.getTop() * scaledSize,
                    bottom = glyph.planeBounds.getBottom() * scaledSize,
                    atlasW = atlasWidth,
                    atlasH = atlasHeight;

            float x0 = x + left,
                    x1 = x + right,
                    y0 = y + (ascender - top),
                    y1 = y + (ascender - bottom),
                    u0 = glyph.atlasBounds.getLeft() / atlasW,
                    u1 = glyph.atlasBounds.getRight() / atlasW,
                    v0 = glyph.atlasBounds.getTop() / atlasH,
                    v1 = glyph.atlasBounds.getBottom() / atlasH;

            y0 += metrics.underlineY * size;
            y1 += metrics.underlineY * size;

            bufferBuilder.vertex( x0, y0, 0);
            bufferBuilder.texture(u0, v0).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).next();
            bufferBuilder.vertex( x0, y1, 0);
            bufferBuilder.texture(u0, v1).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).next();
            bufferBuilder.vertex( x1 , y1, 0);
            bufferBuilder.texture(u1, v1).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).next();
            bufferBuilder.vertex( x1, y0, 0);
            bufferBuilder.texture(u1, v0).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).next();

            x += glyph.advance * size;
            colorOffset += 150;
        }

        shader.bind(atlasId);
        BufferRenderer.draw(bufferBuilder.end());
        shader.unbind();

        RenderSystem.disableBlend();
    }

    @Override
    public void drawCharRotated(char c, float x, float y, float rotate, Color color, float size, DrawContext context) {
        SDFShader shader = RenderUtil.getSdfShader();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        float fsScale = (1.0f / (metrics.ascender - metrics.descender)) * size;


        FontGlyph glyph = glyphs.get((int)c);
        if(glyph == null)
        {
            bufferBuilder.end();
            return;
        }

        final float scaledSize = size,
                ascender = metrics.ascender * scaledSize,
                left = glyph.planeBounds.getLeft() * scaledSize,
                right = glyph.planeBounds.getRight() * scaledSize,
                top = glyph.planeBounds.getTop() * scaledSize,
                bottom = glyph.planeBounds.getBottom() * scaledSize,
                atlasW = atlasWidth,
                atlasH = atlasHeight;

        float x0 = x + left,
                x1 = x + right,
                y0 = y + (ascender - top),
                y1 = y + (ascender - bottom),
                u0 = glyph.atlasBounds.getLeft() / atlasW,
                u1 = glyph.atlasBounds.getRight() / atlasW,
                v0 = glyph.atlasBounds.getTop() / atlasH,
                v1 = glyph.atlasBounds.getBottom() / atlasH;

        y0 += metrics.underlineY * size;
        y1 += metrics.underlineY * size;

        bufferBuilder.vertex( x0, y0, 0);
        bufferBuilder.texture(u0, v0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex( x0, y1, 0);
        bufferBuilder.texture(u0, v1).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex( x1 , y1, 0);
        bufferBuilder.texture(u1, v1).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();
        bufferBuilder.vertex( x1, y0, 0);
        bufferBuilder.texture(u1, v0).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).next();

        shader.bind(atlasId, color, new Vector2f(x + (size * 0.5f),y  + (size * 0.5f)), rotate);
        BufferRenderer.draw(bufferBuilder.end());
        shader.unbind();

        RenderSystem.disableBlend();
    }

    public final void drawString(String text, float x, float y, float size, Color c, DrawContext context)
    {
        SDFShader shader = RenderUtil.getSdfShader();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        for(final char character : text.toCharArray())
        {
            float fsScale = (1.0f / (metrics.ascender - metrics.descender)) * size;

            if(character == ' ')
                x += fsScale * 0.25f;

            FontGlyph glyph = glyphs.get((int)character);
            if(glyph == null)
                continue;

            final float scaledSize = size,
                    ascender = metrics.ascender * scaledSize,
                    left = glyph.planeBounds.getLeft() * scaledSize,
                    right = glyph.planeBounds.getRight() * scaledSize,
                    top = glyph.planeBounds.getTop() * scaledSize,
                    bottom = glyph.planeBounds.getBottom() * scaledSize,
                    atlasW = atlasWidth,
                    atlasH = atlasHeight;

            float x0 = x + left,
                    x1 = x + right,
                    y0 = y + (ascender - top),
                    y1 = y + (ascender - bottom),
                    u0 = glyph.atlasBounds.getLeft() / atlasW,
                    u1 = glyph.atlasBounds.getRight() / atlasW,
                    v0 = glyph.atlasBounds.getTop() / atlasH,
                    v1 = glyph.atlasBounds.getBottom() / atlasH;

            y0 += metrics.underlineY * size;
            y1 += metrics.underlineY * size;

            bufferBuilder.vertex( x0, y0, 0);
            bufferBuilder.texture(u0, v0).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).next();
            bufferBuilder.vertex( x0, y1, 0);
            bufferBuilder.texture(u0, v1).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).next();
            bufferBuilder.vertex( x1 , y1, 0);
            bufferBuilder.texture(u1, v1).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).next();
            bufferBuilder.vertex( x1, y0, 0);
            bufferBuilder.texture(u1, v0).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).next();

            x += glyph.advance * size;
        }

        shader.bind(atlasId, c, new Vector2f(0,0), 0);
        BufferRenderer.draw(bufferBuilder.end());
        shader.unbind();

        RenderSystem.disableBlend();
    }

    public final void drawString(String text, float x, float y, float size, Color c)
    {
        drawString(text,x,y,size,c,null);
    }


    public final float getWidth(String text, float size)
    {
        float x = 0;
        for(final char character : text.toCharArray()) {
            if (character == ' ')
            {
                x += size * 0.25f;
                continue;
            }

            FontGlyph glyph = glyphs.get((int)character);
            if(glyph == null)
                glyph = glyphs.get((int)'?');


            x+= glyph.advance * size;
        }

        return x;
    }

    @Override
    public float getWidthOrdered(OrderedText text, float size) {
        AtomicReference<Float> textX = new AtomicReference<>(0.0f);

        float fsScale = (1.0f / (metrics.ascender - metrics.descender)) * size;

        text.accept((index, style, codePoint) -> {
            if (codePoint > 256) {
                return true;
            }

            if((char)codePoint == ' ')
                textX.set(textX.get() + (fsScale * 0.25f));

            FontGlyph glyph = glyphs.get((int)codePoint);
            if(glyph == null)
                return true;

            textX.set(textX.get() + (glyph.advance * size));
            return true;
        });

        return textX.get();
    }

    public final float getHeight(float size)
    {
        return metrics.lineHeight * size;
    }
}
