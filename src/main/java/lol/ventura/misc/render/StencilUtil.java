package lol.ventura.misc.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.EXTPackedDepthStencil;
import org.lwjgl.opengl.GL11;

public class StencilUtil {
    public static void init()
    {
        EXTFramebufferObject.glDeleteFramebuffersEXT(MinecraftClient.getInstance().getFramebuffer().getDepthAttachment());

        final int id = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, id);
        EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT, MinecraftClient.getInstance().getFramebuffer().textureWidth, MinecraftClient.getInstance().getFramebuffer().textureHeight);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, id);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, id);
    }

    public static void checkInited()
    {
        Framebuffer fb = MinecraftClient.getInstance().getFramebuffer();
        if (fb != null) {
            if (fb.getDepthAttachment() > -1) {
                init();
                MinecraftClient.getInstance().getFramebuffer().depthAttachment = -1;
            }
        }
    }

    public static void bindWrite()
    {
        checkInited();
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);

        GL11.glColorMask(false, false, false, false);
    }

    public static void bindRead() {
        GL11.glColorMask(true, true, true, true);
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
    }

    public static void bindReadExclude() {
        GL11.glColorMask(true, true, true, true);
        GL11.glStencilFunc( GL11.GL_NOTEQUAL, 1, 0xFF);
        GL11.glStencilOp( GL11.GL_KEEP,  GL11.GL_KEEP,  GL11.GL_KEEP);
    }

    public static void disable()
    {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

}
