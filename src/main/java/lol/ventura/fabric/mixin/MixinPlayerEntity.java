package lol.ventura.fabric.mixin;

import lol.ventura.foundation.rotation.Rotation;
import lol.ventura.foundation.rotation.RotationService;
import lol.ventura.foundation.rotation.planner.RotationPlan;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends MixinLivingEntity {
    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getYaw()F"))
    private float hookFixRotation(PlayerEntity entity) {
        RotationService rotationService = RotationService.getInstance();
        Rotation rotation = rotationService.getCurrentRotation();
        RotationPlan plan = rotationService.getStoredRotationPlan();

        if (plan == null || !plan.isApplyVelocityFix() || rotation == null) {
            return entity.getYaw();
        }

        return rotation.getYaw();
    }

    @Redirect(method = "tickNewAi", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getYaw()F"))
    private float hookHeadRotations(PlayerEntity instance) {
        if ((Object) this != MinecraftClient.getInstance().player) {
            return instance.getYaw();
        }

        Pair<Float, Float> pitch = RotationService.getInstance().getRotationPitch();
        RotationService rotations = RotationService.getInstance();
        Rotation rotation = rotations.displayRotations();
        RotationPlan plan = rotations.getStoredRotationPlan();

        rotations.setRotationPitch(new Pair<>(pitch.getRight(), rotation.getPitch()));

        return (rotations.getCurrentRotation() != null || plan != null) ? rotation.getYaw() : instance.getYaw();
    }
}