package lol.ventura.fabric.mixin;

import lol.ventura.features.modules.render.Interface;
import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.module.ModuleRepository;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouse implements GameAccessor {
    @Inject(method = "onMouseButton", at = @At("HEAD"))
    public void onMouseButtonHook(long window, int button, int action, int mods, CallbackInfo ci) {
        if (window == mc.getWindow().getHandle()) {
            ModuleRepository.getInstance().getModule(Interface.class).leftIsDown = action == 0;
        }
    }
}