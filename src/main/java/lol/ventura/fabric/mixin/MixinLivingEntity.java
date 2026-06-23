package lol.ventura.fabric.mixin;

import lol.ventura.foundation.rotation.Rotation;
import lol.ventura.foundation.rotation.RotationService;
import lol.ventura.foundation.rotation.planner.RotationPlan;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends MixinEntity {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"), slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F", ordinal = 1)))
    private float hookBodyRotationsA(LivingEntity instance) {
        if ((Object) this != MinecraftClient.getInstance().player) {
            return instance.getYaw();
        }

        RotationService rotations = RotationService.getInstance();
        Rotation rotation = rotations.getCurrentRotation();
        RotationPlan plan = rotations.getStoredRotationPlan();

        return (rotation != null && plan != null) ? rotation.getYaw() : instance.getYaw();
    }

    @Redirect(method = "turnHead", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"))
    private float hookBodyRotationsB(LivingEntity instance) {
        if ((Object) this != MinecraftClient.getInstance().player) {
            return instance.getYaw();
        }

        RotationService rotations = RotationService.getInstance();
        Rotation rotation = rotations.getCurrentRotation();
        RotationPlan plan = rotations.getStoredRotationPlan();

        return (rotation != null && plan != null) ? rotation.getYaw() : instance.getYaw();
    }


    @Redirect(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d hookFixJump(Vec3d instance, double x, double y, double z) {
        RotationService rotationService = RotationService.getInstance();
        Rotation rotation = rotationService.getCurrentRotation();
        RotationPlan plan = rotationService.getStoredRotationPlan();

        if ((Object) this != MinecraftClient.getInstance().player) {
            return instance.add(x, y, z);
        }

        if (plan == null || !plan.isApplyVelocityFix() || rotation == null) {
            return instance.add(x, y, z);
        }

        float yaw = rotation.getYaw() * 0.017453292F;

        return instance.add(-MathHelper.sin(yaw) * 0.2F, 0.0, MathHelper.cos(yaw) * 0.2F);
    }

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getPitch()F"))
    private float hookModifyFallFlyingPitch(LivingEntity instance) {
        RotationService rotationService = RotationService.getInstance();
        Rotation rotation = rotationService.getCurrentRotation();
        RotationPlan plan = rotationService.getStoredRotationPlan();

        if (instance != MinecraftClient.getInstance().player || rotation == null || plan == null || !plan.isApplyVelocityFix() || plan.isChangeLook()) {
            return instance.getPitch();
        }

        return rotation.getPitch();
    }

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d hookModifyFallFlyingRotationVector(LivingEntity instance) {
        RotationService rotationService = RotationService.getInstance();
        Rotation rotation = rotationService.getCurrentRotation();
        RotationPlan plan = rotationService.getStoredRotationPlan();

        if (instance != MinecraftClient.getInstance().player || rotation == null || plan == null || !plan.isApplyVelocityFix() || plan.isChangeLook()) {
            return instance.getRotationVector();
        }

        return rotation.getRotationVec();
    }
}