package lol.ventura.fabric.mixin;

import lol.ventura.misc.render.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class MixinScreen {
    @Shadow
    public int width;

    @Shadow
    public int height;

    private int oldWidth = 0;
    private int oldHeight = 0;

    @Inject(method = "renderBackground", at = @At("RETURN"))
    private void renderBackground(DrawContext context, CallbackInfo ci)
    {
        if(MinecraftClient.getInstance().world != null)
        {
            RenderUtil.drawBlurRoundedRect(context.getMatrices(),0,0,oldWidth, oldHeight,1);
            RenderUtil.getBlurShader().draw(context.getMatrices());

            width = oldWidth;
            height = oldHeight;
        }
    }


    @Inject(method = "renderBackground", at = @At("HEAD"))
    private void renderBackground2(DrawContext context, CallbackInfo ci)
    {
        if(MinecraftClient.getInstance().world != null)
        {
            oldWidth = width;
            oldHeight = height;

            width = 0;
            height = 0;
        }
    }
}
