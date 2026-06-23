package lol.ventura.misc.render.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lol.ventura.foundation.GameAccessor;
import lol.ventura.misc.render.GLObject;
import lol.ventura.misc.render.RenderUtil;
import lombok.AllArgsConstructor;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Shader extends GLObject implements GameAccessor {
    public static class Program extends GLObject
    {
        private int type;

        public static Program create(int type)
        {
            Program program = new Program();
            program.type = type;
            program.setHandle(GlStateManager.glCreateShader(type));

            return program;
        }

        public Program setSource(String source)
        {
            GlStateManager.glShaderSource(getHandle(), Collections.singletonList(source));
            return this;
        }

        public Program compile()
        {
            GlStateManager.glCompileShader(getHandle());

            if (GlStateManager.glGetShaderi(getHandle(), GL20.GL_COMPILE_STATUS) == 0) {
                throw new RuntimeException("Error compiling Shader code: " + GlStateManager.glGetShaderInfoLog(getHandle(), 1024));
            }

            return this;
        }
    }

    @AllArgsConstructor
    public static class UniformAccessor {
        private int id;

        public void setFloat(float f)
        {
            GL20.glUniform1f(id, f);
        }

        public void setFloat2(Vector2f vec)
        {
            GL20.glUniform2f(id,vec.x,vec.y);
        }

        public void setFloat4(Vector4f vec)
        {
            GL20.glUniform4f(id,vec.x,vec.y,vec.z,vec.w);
        }

        public void setColor(Color c)
        {
            setFloat4(new Vector4f(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, c.getAlpha() / 255.0f));
        }

        public void setInt(int i)
        {
            GL20.glUniform1i(id, i);
        }

        public void setMatrix4(Matrix4f matrix)
        {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer buffer = stack.mallocFloat(16);
                matrix.get(buffer);

                GL20.glUniformMatrix4fv(id, false, buffer);
            }
        }

        public void setFloatBuffer(float[] buffer)
        {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer memBuffer = stack.mallocFloat(buffer.length);
                memBuffer.put(buffer);
                memBuffer.flip();

                GL20.glUniform1fv(id, memBuffer);
            }
        }
    }

    private HashMap<Integer, Program> programs = new HashMap<>();
    protected UniformAccessor model, projection;

    public Shader()
    {
        setHandle(GlStateManager.glCreateProgram());
    }

    protected void addProgram(Program program)
    {
        programs.put(program.type, program);
    }

    protected void bindMatrices()
    {
        model.setMatrix4(RenderSystem.getModelViewMatrix());
        projection.setMatrix4(RenderSystem.getProjectionMatrix());
    }

    protected void bindMatrices(Matrix4f custom)
    {
        Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewMatrix());
        model.setMatrix4(modelView.mul(custom));
        projection.setMatrix4(RenderSystem.getProjectionMatrix());
    }

    protected UniformAccessor getUniform(String name)
    {
        return new UniformAccessor(GlStateManager._glGetUniformLocation(getHandle(), name));
    }

    protected void link()
    {
        for(Map.Entry<Integer, Program> entry : programs.entrySet())
        {
            GlStateManager.glAttachShader(getHandle(), entry.getValue().getHandle());
        }

        GlStateManager.glLinkProgram(getHandle());

        if (GlStateManager.glGetProgrami(getHandle(), GL20.GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Error compiling Shader code: " + GlStateManager.glGetProgramInfoLog(getHandle(), 1024));
        }

        model = getUniform("model");
        projection =getUniform("projection");
    }

}
