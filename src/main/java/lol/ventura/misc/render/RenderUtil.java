package lol.ventura.misc.render;

import com.mojang.blaze3d.systems.RenderSystem;
import lol.ventura.features.shaders.*;
import lol.ventura.misc.math.Bounds;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.awt.*;

public class RenderUtil {

    private static RoundedShader roundedShader;
    private static RoundedOutlineShader outlineRoundedShader;
    private static TexturedRoundedShader texturedRoundedShader;
    private static GradientRoundedShader gradientRoundedShader;
    private static HeadRoundedShader headRoundedShader;

    @Getter
    private static SDFShader sdfShader;

    @Getter
    private static GradientSDFShader gradientSDFShader;

    @Getter
    private static BlurShader blurShader;

    @Getter
    private static BloomShader preBloomShader, postBloomShader;

    @Getter
    private static SpiralShader spiralShader;

    @Getter @Setter
    private static Matrix4f projectionMatrix = new Matrix4f();
    @Getter @Setter
    private static  Matrix4f modelMatrix = new Matrix4f();
    @Getter @Setter
    private static Matrix4f worldMatrix = new Matrix4f();

    public static void init()
    {
        roundedShader = new RoundedShader();
        sdfShader = new SDFShader();
        blurShader = new BlurShader();
        outlineRoundedShader = new RoundedOutlineShader();
        preBloomShader = new BloomShader();
        postBloomShader = new BloomShader();
        texturedRoundedShader = new TexturedRoundedShader();
        gradientRoundedShader = new GradientRoundedShader();
        headRoundedShader = new HeadRoundedShader();
        gradientSDFShader = new GradientSDFShader();
        spiralShader = new SpiralShader();
    }

    public static void drawPreBloom(BloomShader.Drawable drawable)
    {
        preBloomShader.drawCalls.add(drawable);
    }

    public static void drawPostBloom(BloomShader.Drawable drawable)
    {
        postBloomShader.drawCalls.add(drawable);
    }

    public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, Color c, BufferBuilder bufferBuilder) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(c.getRGB()).next();
        bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(c.getRGB()).next();
        bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(c.getRGB()).next();
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(c.getRGB()).next();
    }

    public static void drawRectFan(MatrixStack matrices, float x, float y, float width, float height, Color c, BufferBuilder bufferBuilder) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float centerX = x + width / 2;
        float centerY = y + height / 2;

        bufferBuilder.vertex(matrix, centerX, centerY, 0.0F).color(c.getRGB()).next();

        bufferBuilder.vertex(matrix, x, y, 0.0F).color(c.getRGB()).next();
        bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(c.getRGB()).next();
        bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(c.getRGB()).next();
        bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(c.getRGB()).next();

        bufferBuilder.vertex(matrix, centerX, centerY, 0.0F).color(c.getRGB()).next();

    }

    public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, Color c) {
        DiffuseLighting.disableGuiDepthLighting();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        drawRect(matrices,x,y,width,height,c,bufferBuilder);
        Tessellator.getInstance().draw();
        RenderSystem.disableBlend();
    }

    public static void drawRoundedRectInternalFan(Matrix4f matrix, float cr, float cg, float cb, float ca,
                                               double fromX, double fromY, double toX, double toY,
                                               double radC1, double radC2, double radC3, double radC4,
                                               double samples, BufferBuilder bufferBuilder) {

        // Array to map each corner's position and radius
        double[][] map = new double[][] {
                { toX - radC4, toY - radC4, radC4 }, // Bottom-right corner
                { toX - radC2, fromY + radC2, radC2 }, // Top-right corner
                { fromX + radC1, fromY + radC1, radC1 }, // Top-left corner
                { fromX + radC3, toY - radC3, radC3 }  // Bottom-left corner
        };



        // Loop over each corner and add its vertices in the same buffer
        for (int i = 0; i < 4; i++) {
            double[] current = map[i];
            double rad = current[2];

            // Center of the corner
            float centerX = (float) current[0];
            float centerY = (float) current[1];

            // First vertex in the triangle fan is always the center of the arc (corner center)
            bufferBuilder.vertex(matrix, centerX, centerY, 0.0F).color(cr, cg, cb, ca).next();

            // Loop through each angle to create vertices around the arc
            for (double r = i * 90d; r < (360 / 4d + i * 90d); r += (90 / samples)) {
                // Calculate the angle and corresponding vertex on the arc
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);

                // Calculate the x and y positions for the current arc vertex
                float x = centerX + sin;
                float y = centerY + cos;

                // Add the vertex to the triangle fan
                bufferBuilder.vertex(matrix, x, y, 0.0F).color(cr, cg, cb, ca).next();
            }

            // Close the fan by adding the last vertex and closing the arc
            float rad1 = (float) Math.toRadians(360 / 4d + i * 90d);
            float sin = (float) (Math.sin(rad1) * rad);
            float cos = (float) (Math.cos(rad1) * rad);
            float x = centerX + sin;
            float y = centerY + cos;

            // Final vertex to close the corner's arc
            bufferBuilder.vertex(matrix, x, y, 0.0F).color(cr, cg, cb, ca).next();
        }


    }




    public static void drawRoundedRectInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double radC1, double radC2, double radC3, double radC4, double samples, BufferBuilder bufferBuilder)
    {
        DiffuseLighting.disableGuiDepthLighting();
        double[][] map = new double[][] { new double[] { toX - radC4, toY - radC4, radC4 }, new double[] { toX - radC2, fromY + radC2, radC2 },
                new double[] { fromX + radC1, fromY + radC1, radC1 }, new double[] { fromX + radC3, toY - radC3, radC3 } };
        for (int i = 0; i < 4; i++) {
            double[] current = map[i];
            double rad = current[2];
            for (double r = i * 90d; r < (360 / 4d + i * 90d); r += (90 / samples)) {
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);
                bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
            }
            float rad1 = (float) Math.toRadians((360 / 4d + i * 90d));
            float sin = (float) (Math.sin(rad1) * rad);
            float cos = (float) (Math.cos(rad1) * rad);
            bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
        }

    }

    //to juz akurat nie $$$$elfcodded ale to z kodu ktory mial byc do przeniesienia wiec mozna.
    private static void drawRoundedRectInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double radC1, double radC2, double radC3, double radC4, double samples) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        drawRoundedRectInternal(matrix,cr,cg,cb,ca,fromX,fromY,toX,toY,radC1,radC2,radC3,radC4,samples,bufferBuilder);

        Tessellator.getInstance().draw();
        RenderSystem.disableBlend();
    }

    public static void drawRoundedRectDepthed(MatrixStack stack, float x, float y, float width, float height, float radius, Color color)
    {
        drawRoundedRectInternal(stack.peek().getPositionMatrix(), color.getRed() / 255.0f, color.getGreen()/ 255.0f, color.getBlue()/ 255.0f, color.getAlpha()/ 255.0f, x,y,x+width,y+height,radius,radius,radius,radius,4);
    }

    public static void drawRoundedRectDepthedTwoRadius(MatrixStack stack, float x, float y, float width, float height, float lRadius, float rRadius, Color color)
    {
        drawRoundedRectInternal(stack.peek().getPositionMatrix(), color.getRed() / 255.0f, color.getGreen()/ 255.0f, color.getBlue()/ 255.0f, color.getAlpha()/ 255.0f, x,y,x+width,y+height,lRadius, rRadius, lRadius,rRadius,4);
    }

    public static void drawBlurRoundedRect(MatrixStack stack, float x, float y, float width, float height, float radius)
    {
        blurShader.drawCalls.add(new BlurShader.DrawCall(BlurShader.PrimitiveType.RECTANGLE, radius, radius, new Bounds(x,y,width,height)));
    }


    public static void drawBlurRoundedRectWithTintBordered(
            MatrixStack stack,
            float x, float y, float width, float height,
            float radius, Color tint, Color border, float thickness)
    {
        blurShader.drawCalls.add(new BlurShader.DrawCall(BlurShader.PrimitiveType.RECTANGLE, radius, radius, new Bounds(x+thickness,y+thickness,width-thickness-thickness,height-thickness-thickness)));
       // RenderUtil.drawOutlineRoundedRect(x+thickness,y+thickness,width-thickness,height-thickness,radius,tint);

    }

    public static void drawBlurRoundedRectWithTint(MatrixStack stack, float x, float y, float width, float height, float radius, Color tint)
    {
        blurShader.drawCalls.add(new BlurShader.DrawCall(BlurShader.PrimitiveType.RECTANGLE, radius, radius, new Bounds(x,y,width,height)));
        RenderUtil.drawRoundedRect(x,y,width,height,radius,tint);
    }

    public static void drawTexturedRect(MatrixStack matrices, float x, float y, float width, float height)
    {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix, x, y + height, 0.0F).texture(0,1).next();
        bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).texture(1,1).next();
        bufferBuilder.vertex(matrix, x + width, y, 0.0F).texture(1,0).next();
        bufferBuilder.vertex(matrix, x, y, 0.0F).texture(0,0).next();
        Tessellator.getInstance().draw();
        RenderSystem.disableBlend();
    }


    public static void drawRoundedRect(float x, float y, float width, float height, float radius, Color c)
    {
        roundedShader.drawRect(x,y,width,height,radius,c);
    }

    public static void drawTexturedRoundedRect(float x, float y, float width, float height, float radius, int texture)
    {
       drawTexturedRoundedRect(x,y,width,height,radius,texture,false);
    }

    public static void drawTexturedRoundedRect(float x, float y, float width, float height, float radius, int texture, boolean flipUV)
    {
        texturedRoundedShader.drawRect(x,y,width,height,radius,texture, flipUV);
    }

    public static void drawHeadRoundedRect(float x, float y, float width, float height, float radius, int texture)
    {
        headRoundedShader.drawRect(x,y,width,height,radius,texture);
    }

    public static void drawOutlineRoundedRect(float x, float y, float width, float height, float radius, Color c, float thickness)
    {
        outlineRoundedShader.drawRect(x,y,width,height,radius,c,thickness);
    }

    public static void drawGradientRoundedRect(float x, float y, float width, float height, float radius, Color c, Color c2)
    {
        gradientRoundedShader.drawRect(x,y,width,height,radius,c,c2);
    }

}
