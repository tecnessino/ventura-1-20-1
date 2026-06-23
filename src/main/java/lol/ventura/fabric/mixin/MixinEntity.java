package lol.ventura.fabric.mixin;

import lol.ventura.features.events.PlayerVelocityStrafeEvent;
import lol.ventura.foundation.event.EventBus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class MixinEntity {
    @Shadow
    public static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
        return null;
    }

    @Redirect(method = "updateVelocity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;movementInputToVelocity(Lnet/minecraft/util/math/Vec3d;FF)Lnet/minecraft/util/math/Vec3d;"))
    public Vec3d hookVelocity(Vec3d movementInput, float speed, float yaw) {
        if ((Object) this == MinecraftClient.getInstance().player) {
            PlayerVelocityStrafeEvent event = new PlayerVelocityStrafeEvent(movementInput, MixinEntity.movementInputToVelocity(movementInput, speed, yaw), speed, yaw);
            EventBus.getInstance().emit(event);
            return event.getVelocity();
        }

        return MixinEntity.movementInputToVelocity(movementInput, speed, yaw);
    }
}
