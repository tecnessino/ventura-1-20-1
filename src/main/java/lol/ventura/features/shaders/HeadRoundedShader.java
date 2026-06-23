package lol.ventura.features.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lol.ventura.misc.render.shaders.Shader;
import net.minecraft.client.render.*;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

public class HeadRoundedShader extends Shader {
    public HeadRoundedShader()
    {
        addProgram(Program.create(GL20.GL_VERTEX_SHADER).setSource("""
                #version 150
                in vec3 in_Position;
                in vec2 in_TexCoord;
                in vec4 in_Color;
                
                 uniform mat4 model;
                 uniform mat4 projection;
                
                 out vec2 texCoord;
                 out vec2 sampleTexCoord;
                
                 void main() {
                     gl_Position = projection * model * vec4(in_Position, 1.0);
                     texCoord = in_Color.xy;
                     sampleTexCoord = in_TexCoord;
                 }
                """).compile());
        addProgram(Program.create(GL20.GL_FRAGMENT_SHADER).setSource("#version 150\n" +
                "in vec2 texCoord;\n" +
                "in vec2 sampleTexCoord;\n" +
                "uniform vec2 size;\n" +
                "uniform float radius;\n" +
                "uniform sampler2D tex;\n" +
                "\n" +
                "float calc(vec2 p, vec2 b, float r) {\n" +
                "   return length(max(abs(p) - b, 0.0)) - r;\n" +
                "}\n" +
                "\n" +
                "out vec4 FragColor;\n" +
                "\n" +
                "void main() {\n" +
                "vec4 color = texture(tex, sampleTexCoord);\n" +
                "vec2 pixel = texCoord * size;\n" +
                "vec2 centre = 0.5 * size;\n" +
                "float sa = smoothstep(0.0, 1, calc(centre - pixel, centre - radius - 1, radius));\n" +
                "vec4 c = mix(vec4(color.rgb, 1), vec4(color.rgb, 0), sa);\n" +
                "FragColor = vec4(c.rgb, color.a * c.a);\n" +
                "}").compile());
        link();

        size = getUniform("size");
        radius = getUniform("radius");
        color = getUniform("color");
    }

    private UniformAccessor size,radius,color;

    public void drawRect(float x, float y, float width, float height, float r, int texture)
    {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        GlStateManager._glUseProgram(getHandle());

        bindMatrices();
        radius.setFloat(r);
        size.setFloat2(new Vector2f(width,height));
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager._bindTexture(texture);

        float textureWidth = 64;
        float textureHeight = 64;
        Vector2f uvTopLeft = new Vector2f(8 / textureWidth,8 / textureHeight);
        Vector2f uvBottomRight = new Vector2f(16 / textureHeight, 16 / textureHeight);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        bufferBuilder.vertex( x, y + height, 0.0F).texture(uvTopLeft.x, uvBottomRight.y).color(0,255,0,0).next();
        bufferBuilder.vertex( x + width, y + height, 0.0F).texture(uvBottomRight.x, uvBottomRight.y).color(255,255,0,0).next();
        bufferBuilder.vertex( x + width, y, 0.0F).texture(uvBottomRight.x, uvTopLeft.y).color(255,0,0,0).next();
        bufferBuilder.vertex( x, y, 0.0F).texture(uvTopLeft.x,uvTopLeft.y).color(0,0,0,0).next();

        BufferRenderer.draw(bufferBuilder.end());

        GlStateManager._glUseProgram(0);

        RenderSystem.disableBlend();
    }
}
