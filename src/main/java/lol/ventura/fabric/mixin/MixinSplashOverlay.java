package lol.ventura.fabric.mixin;

import lol.ventura.features.modules.render.Interface;
import lol.ventura.features.shaders.BloomShader;
import lol.ventura.misc.animation.Animation;
import lol.ventura.misc.font.FontRepository;
import lol.ventura.misc.render.RenderUtil;
import lol.ventura.misc.render.ScaledResolution;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.resource.ResourceReload;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(SplashOverlay.class)
public class MixinSplashOverlay {
    @Shadow @Final
    private ResourceReload reload;

    public Animation animation = new Animation();
    public Animation slider = new Animation();
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;setShaderColor(FFFF)V", shift = At.Shift.AFTER, ordinal = 1))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci)
    {
        context.fill(0,0, (int)ScaledResolution.getWidth(), (int)ScaledResolution.getHeight(), Color.black.getRGB());

        {
            float x= (ScaledResolution.getWidth() *0.5f)  - (95 * 0.5f);
            float y = (int)ScaledResolution.getHeight() - 60;

            slider.update();
            slider.animate(reload.getProgress() * 95, 0.1f);

            RenderUtil.drawRoundedRect(x,y,95,2,0.1f,new Color(133,133,133));
            RenderUtil.drawRoundedRect(x,y, (float) slider.getValue(),2,0.1f,new Color(255,255,255));
        }


        if(!FontRepository.getInstance().hasFont("poppins"))
            return;

        animation.update();
        animation.animate(255, 0.25f);

        float x= (ScaledResolution.getWidth() *0.5f) - (FontRepository.getInstance().getFont("poppins").getWidth("Ventura",16) * 0.5f);

        float x2= (ScaledResolution.getWidth() *0.5f) - (FontRepository.getInstance().getFont("poppins").getWidth("Made in Mongolia",8) * 0.5f);
        float y = (ScaledResolution.getHeight() *0.5f) - Interface.GlobalFont.POPPINS.getFixedHalfHeight();


        FontRepository.getInstance().getFont("poppins").drawString("Ventura",x,y,16, new Color(255,255,255, (int) animation.getValue()), context);
        FontRepository.getInstance().getFont("poppins").drawString("Made in Mongolia",x2,y+18,8, new Color(200,200,200, (int) animation.getValue()), context);

    }

    @Inject(method = "renderProgressBar", at = @At(value = "HEAD"), cancellable = true)
    private void progressbar(DrawContext drawContext, int minX, int minY, int maxX, int maxY, float opacity, CallbackInfo ci)
    {
        ci.cancel();
    }

}
