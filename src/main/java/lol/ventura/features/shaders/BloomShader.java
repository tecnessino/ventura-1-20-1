package lol.ventura.features.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lol.ventura.misc.math.Bounds;
import lol.ventura.misc.render.FramebufferManager;
import lol.ventura.misc.render.RenderUtil;
import lol.ventura.misc.render.shaders.Shader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector2f;
import org.lwjgl.opengl.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BloomShader extends Shader {

    public static interface Drawable
    {
        void draw(MatrixStack matrix);
    }


    public BloomShader()
    {
        addProgram(Program.create(GL20.GL_VERTEX_SHADER).setSource("""
                #version 150
                in vec3 in_Position;
                in vec2 in_TexCoord;
                
                 uniform mat4 model;
                 uniform mat4 projection;
                
                 out vec2 texCoord;
                
                 void main() {
                     gl_Position = vec4(in_Position, 1.0);
                     texCoord = in_TexCoord;
                 }
                """).compile());
        addProgram(Program.create(GL20.GL_FRAGMENT_SHADER).setSource("""
#version 150
in vec2 texCoord;

uniform sampler2D lwc;
out vec4 FragColor;

uniform vec2 resolution;
uniform vec2 direction;
uniform int blurRad;
uniform float weight[64]; //max rad is 64, if you want more just increase it here and somewhere in code

void main() {
    vec2 fragSize = 1.0 / resolution.xy;
    vec4 texSample = texture(lwc, texCoord);
    vec3 color = texSample.rgb * weight[0];
    float alpha = texSample.a * weight[0];

    for(int i = 1; i < blurRad; ++i) {
        vec2 blurOffset = vec2(
            (fragSize.x * float(i)) * direction.x,
            (fragSize.y * float(i)) * direction.y
        );

        vec4 texSample1 = texture(lwc, texCoord + blurOffset);
        vec4 texSample2 = texture(lwc, texCoord - blurOffset);
        color += texSample1.rgb * weight[i];
        color += texSample2.rgb * weight[i];
        alpha += texSample1.a * weight[i];
        alpha += texSample2.a * weight[i];
    }

    alpha = clamp(alpha, 0.0, 1.0); //2025 optimalization wdmo
    FragColor = vec4(color / alpha, alpha);
}

"""
        ).compile());
        link();

        resolution = getUniform("resolution");
        direction = getUniform("direction");
        rad = getUniform("blurRad");
        weights = getUniform("weight");
    }

    public int radius = 20;
    private FramebufferManager.FramebufferEntry inputBuffer, vBuffer;
    private final UniformAccessor resolution, direction, rad, weights;
    private Vector2f prevRes = new Vector2f(0);
    private int prevRad;
    private float[] weight;
    public final List<Drawable> drawCalls = new ArrayList<>();

    public static float[] generateGaussianWeights(int radius) {
        float[] xxx = new float[radius+1];
        for(int i = 0; i <= radius; i++)
        {
            float sigma = radius * 0.5f;
            double output = 1.0 / Math.sqrt(2.0 * Math.PI * (sigma * sigma));
            xxx[i] = (float) (output * Math.exp(-(i * i) / (2.0 * (sigma * sigma))));
        }

        return xxx;
    }

    public void draw(MatrixStack stack)
    {
        int resX = mc.getFramebuffer().textureWidth;
        int resY = mc.getFramebuffer().textureHeight;

        int halfResX = resX;
        int halfResY = resY;

        if(vBuffer == null)
            vBuffer = FramebufferManager.getInstance().getFramebuffer(false);

        if(inputBuffer == null)
            inputBuffer = FramebufferManager.getInstance().getFramebuffer(false);

        inputBuffer.getFramebuffer().clear(true);
        inputBuffer.getFramebuffer().beginWrite(true);

        for(Drawable call : drawCalls)
        {
            call.draw(stack);
        }

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if(prevRad != radius)
        {
            weight = generateGaussianWeights(radius);
            prevRad = radius;
        }

        {
            vBuffer.getFramebuffer().clear(true);
            vBuffer.getFramebuffer().beginWrite(true);

            GlStateManager._glUseProgram(getHandle());

            bindMatrices();
            direction.setFloat2(new Vector2f(1,0));
            resolution.setFloat2(new Vector2f(vBuffer.getFramebuffer().textureWidth, vBuffer.getFramebuffer().textureHeight));
            rad.setInt(radius);
            weights.setFloatBuffer(weight);

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            inputBuffer.getFramebuffer().beginRead();


            bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE);

            bufferBuilder.vertex(-1,-1,0).texture(0,0).next();
            bufferBuilder.vertex(1,-1, 0).texture(1,0).next();
            bufferBuilder.vertex(1,1,0).texture(1,1).next();
            bufferBuilder.vertex(1,1,0).texture(1,1).next();
            bufferBuilder.vertex(-1,1, 0).texture(0,1).next();
            bufferBuilder.vertex(-1,-1,0).texture(0,0).next();

            BufferRenderer.draw(bufferBuilder.end());

            GlStateManager._glUseProgram(0);
        }

        {
            mc.getFramebuffer().beginWrite(true);

            GlStateManager._glUseProgram(getHandle());

            bindMatrices();
            direction.setFloat2(new Vector2f(0,1));
            resolution.setFloat2(new Vector2f(vBuffer.getFramebuffer().textureWidth, vBuffer.getFramebuffer().textureHeight));
            rad.setInt(radius);
            weights.setFloatBuffer(weight);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            vBuffer.getFramebuffer().beginRead();


            bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_TEXTURE);

            bufferBuilder.vertex(-1,-1,0).texture(0,0).next();
            bufferBuilder.vertex(1,-1, 0).texture(1,0).next();
            bufferBuilder.vertex(1,1,0).texture(1,1).next();
            bufferBuilder.vertex(1,1,0).texture(1,1).next();
            bufferBuilder.vertex(-1,1, 0).texture(0,1).next();
            bufferBuilder.vertex(-1,-1,0).texture(0,0).next();

            BufferRenderer.draw(bufferBuilder.end());

            GlStateManager._glUseProgram(0);
        }


        RenderSystem.disableBlend();
        drawCalls.clear();

    }
}
