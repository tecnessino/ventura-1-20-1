package lol.ventura.fabric.mixin;

import lol.ventura.features.events.GameTickEvent;
import lol.ventura.features.events.GameWorldChangeEvent;
import lol.ventura.features.modules.player.SafeWalk;
import lol.ventura.foundation.event.EventBus;
import lol.ventura.foundation.module.ModuleRepository;
import lol.ventura.misc.font.FontRepository;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Shadow
    private int itemUseCooldown;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;onResolutionChanged()V"))
    private void onInitializeFont(CallbackInfo ci) {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(FontRepository.getInstance());
    }

    @Inject(method = "setWorld", at = @At("HEAD"))
    private void hookWorldChangeEvent(ClientWorld world, CallbackInfo ci) {
        EventBus.getInstance().emit(new GameWorldChangeEvent(world));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void hookTickEvent(CallbackInfo callbackInfo) {
        EventBus.getInstance().emit(new GameTickEvent());
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if(ModuleRepository.getInstance().getModule(SafeWalk.class).isEnabled() && ModuleRepository.getInstance().getModule(SafeWalk.class).getFastPlace().getValue())
            itemUseCooldown = 0;
    }
}
