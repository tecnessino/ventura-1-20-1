package lol.ventura.features.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import lol.ventura.misc.render.shaders.Shader;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import java.awt.*;

public class GradientSDFShader extends Shader {
    public GradientSDFShader()
    {
        addProgram(Program.create(GL20.GL_VERTEX_SHADER).setSource("""
                #version 150
                in vec3 in_Position;
                in vec2 in_TexCoord;
                in vec4 in_Color;
                
                 uniform mat4 model;
                 uniform mat4 projection;
                
                 out vec2 texCoord;
                 out vec4 color;
                
                 void main() {
                     gl_Position = projection * model * vec4(in_Position, 1.0);
                     texCoord = vec2(in_TexCoord.x, 1 - in_TexCoord.y);
                     color = in_Color;
                 }
                """).compile());
        addProgram(Program.create(GL20.GL_FRAGMENT_SHADER).setSource("""
                #version 150
in vec2 texCoord;
in vec4 color;

uniform sampler2D lwc;
out vec4 FragColor;

float screenPxRange() {
    vec2 unitRange = vec2(16)/vec2(textureSize(lwc, 0));
    vec2 screenTexSize = vec2(1.0)/fwidth(texCoord);
    return max(0.5*dot(unitRange, screenTexSize), 1.0);
}

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

void main() {
    vec3 msd = texture(lwc, texCoord).rgb;
     float sd = median(msd.r, msd.g, msd.b);
     float screenPxDistance = screenPxRange()*(sd - 0.5);
     float opacity = clamp(screenPxDistance + 0.5, 0.0, 1.0);
     
     if(opacity == 0)
        discard;
     
    FragColor = vec4(color.rgb,color.a * opacity);
}
                """).compile());
        link();
    }

    public void bind(int atlas)
    {
        GlStateManager._glUseProgram(getHandle());

        bindMatrices();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL20.glBindTexture(GL20.GL_TEXTURE_2D, atlas);
    }

    public void unbind()
    {
        GlStateManager._glUseProgram(0);
        GL20.glBindTexture(GL20.GL_TEXTURE_2D, 0);
    }
}
