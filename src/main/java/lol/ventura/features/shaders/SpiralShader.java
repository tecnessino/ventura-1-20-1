package lol.ventura.features.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lol.ventura.features.modules.render.Nametags;
import lol.ventura.misc.render.ScaledResolution;
import lol.ventura.misc.render.shaders.Shader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL20;

import java.awt.*;

public class SpiralShader extends Shader {
    public SpiralShader()
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
                uniform vec2 spiralSize;
                uniform float time;
                
                out vec4 FragColor;
                
                void main() {
                    vec2 relPos = texCoord;
                
                    vec2 uv = (relPos - 0.5) * vec2(spiralSize.x / spiralSize.y, 1.0);
                
                    float r = length(uv);
                    float a = atan(uv.y, uv.x);
                    float arms = 3.0;
                    float spin = a + time * 0.3;
                    float spiral = mod(spin - r * 8.0, 6.2831);
                    float intensity = exp(-r * 4.0) * (0.5 + 0.5 * cos(spiral * arms));
                
                    float radial = length(relPos - 0.5);
                    float edgeFade = smoothstep(0.5, 0.4, radial);
                
                    intensity *= edgeFade;
                
                    vec3 color = vec3(0,0,1);
                    FragColor = vec4(color, intensity);
                }
                
                """).compile());
        link();

        spiralSize = getUniform("spiralSize");

        startTime = System.currentTimeMillis();
        time = getUniform("time");
    }

    private UniformAccessor screenSize, spiralPos, spiralSize,time;
    private long startTime;

    public void drawRect(Matrix4f matrix, float x, float y, float z, float width, float height)
    {
        if(mc.currentScreen != null)
            return;

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        GlStateManager._glUseProgram(getHandle());

        spiralSize.setFloat2(new Vector2f((float) (width / MinecraftClient.getInstance().getWindow().getScaleFactor()), (float) (height / MinecraftClient.getInstance().getWindow().getScaleFactor())));
        time.setFloat((System.currentTimeMillis() - startTime) / 250.0f);

        Matrix4f m = new Matrix4f();

        bindMatrices(m);


        Quaternionf quaternionf = new Quaternionf(mc.getEntityRenderDispatcher().camera.getRotation());

        Vector3f[] gowna = {
                new Vector3f(x - 0.5f,y + 1.5f,z-0.5f),
                new Vector3f(x + 0.5f,y + 1.5f,z-0.5f),
                new Vector3f(x + 0.5f,y + 0.5f,z-0.5f),
                new Vector3f(x - 0.5f,y + 0.5f,z-0.5f),
        };

        Vector2f[] coords = {
                new Vector2f(0,1),
                new Vector2f(1,1),
                new Vector2f(1,0),
                new Vector2f(0,0)
        };


        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        for(int i = 0; i < coords.length; i++)
        {
            Vector3f pos = new Vector3f(gowna[i]);
            Vector2f coord = coords[i];


            Vec3d screen = Nametags.convertToScreen(
                    new Vec3d(pos),
                    mc.getEntityRenderDispatcher().camera,
                    mc.getWindow().getHeight(), mc.getWindow().getWidth(),
                    mc.getWindow().getScaleFactor()
            );
            bufferBuilder.vertex(matrix, (float) screen.getX(), (float) screen.getY(),(float) screen.getZ()).texture(coord.x,coord.y).next();
        }

        BufferRenderer.draw(bufferBuilder.end());

        GlStateManager._glUseProgram(0);

        RenderSystem.disableBlend();
    }
}
