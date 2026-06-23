package lol.ventura.features.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lol.ventura.misc.render.shaders.Shader;
import net.minecraft.client.render.*;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import java.awt.*;

public class TexturedRoundedShader extends Shader {
    public TexturedRoundedShader()
    {
        addProgram(Program.create(GL20.GL_VERTEX_SHADER).setSource("""
                #version 150
                in vec3 in_Position;
                in vec2 in_TexCoord;
                
                 uniform mat4 model;
                 uniform mat4 projection;
                
                 out vec2 texCoord;
                
                 void main() {
                     gl_Position = projection * model * vec4(in_Position, 1.0);
                     texCoord = in_TexCoord;
                 }
                """).compile());
        addProgram(Program.create(GL20.GL_FRAGMENT_SHADER).setSource("""
                #version 150
                in vec2 texCoord;
                uniform vec2 size;
                uniform float radius;
                uniform sampler2D tex;
                
                float calc(vec2 p, vec2 b, float r) {
                   return length(max(abs(p) - b, 0.0)) - r;
                }
                
                out vec4 FragColor;
                
                void main() {
                    vec4 color = texture(tex, texCoord);
                    if(color.a < 0.5) discard;
               
                    if(radius == 0)
                    {
                        FragColor = color;
                        return;
                    }
         
                    vec2 pixel = texCoord * size;
                    vec2 centre = 0.5 * size;
                    float sa = smoothstep(0.0, 1, calc(centre - pixel, centre - radius - 1, radius));
                    vec4 c = mix(vec4(color.rgb, 1), vec4(color.rgb, 0), sa);
                    FragColor = vec4(c.rgb, color.a * c.a);
                }""").compile());
        link();

        size = getUniform("size");
        radius = getUniform("radius");
        color = getUniform("color");
    }

    private UniformAccessor size,radius,color;

    public void drawRect(float x, float y, float width, float height, float r, int texture, boolean flipUV)
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


        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        bufferBuilder.vertex( x, y + height, 0.0F).texture(0, flipUV ? 0 : 1).next();
        bufferBuilder.vertex( x + width, y + height, 0.0F).texture(1, flipUV ? 0 : 1).next();
        bufferBuilder.vertex( x + width, y, 0.0F).texture(1, flipUV ? 1 : 0).next();
        bufferBuilder.vertex( x, y, 0.0F).texture(0, flipUV ? 1 : 0).next();

        BufferRenderer.draw(bufferBuilder.end());

        GlStateManager._glUseProgram(0);

        RenderSystem.disableBlend();
    }
}
