package lol.ventura.fabric.mixin;

import lol.ventura.misc.render.FramebufferManager;
import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.util.MonitorTracker;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class MixinWindow {
    @Shadow private int width;
    @Shadow private int height;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(WindowEventHandler eventHandler, MonitorTracker monitorTracker, WindowSettings settings, String videoMode, String title, CallbackInfo ci)
    {
        if(FramebufferManager.getInstance() == null)
            return;

        FramebufferManager.getInstance().handleResize(width, height);
    }

    @Inject(method = "onFramebufferSizeChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/WindowEventHandler;onResolutionChanged()V"))
    private void resize(long window, int width, int height, CallbackInfo ci)
    {
        if(FramebufferManager.getInstance() == null)
            return;

        FramebufferManager.getInstance().handleResize(width, height);
    }
}
