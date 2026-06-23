package lol.ventura.fabric.mixin;

import lol.ventura.features.events.GameMoveInputEvent;
import lol.ventura.foundation.event.EventBus;
import lol.ventura.foundation.rotation.Rotation;
import lol.ventura.foundation.rotation.RotationService;
import lol.ventura.foundation.rotation.planner.RotationPlan;
import lol.ventura.foundation.rotation.movement.DirectionalInput;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput extends MixinInput {

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/input/KeyboardInput;sneaking:Z", shift = At.Shift.AFTER), allow = 1)
    private void injectMovementInputEvent(boolean slowDown, float f, CallbackInfo ci) {
        var event = new GameMoveInputEvent(new DirectionalInput(this.pressingForward, this.pressingBack, this.pressingLeft, this.pressingRight), this.jumping, this.sneaking);
        EventBus.getInstance().emit(event);

        var directionalInput = event.getDirectionalInput();

        this.pressingForward = directionalInput.isForwards();
        this.pressingBack = directionalInput.isBackwards();
        this.pressingLeft = directionalInput.isLeft();
        this.pressingRight = directionalInput.isRight();
        this.movementForward = KeyboardInput.getMovementMultiplier(directionalInput.isForwards(), directionalInput.isBackwards());
        this.movementSideways = KeyboardInput.getMovementMultiplier(directionalInput.isLeft(), directionalInput.isRight());

        this.fixStrafeMovement();

        this.jumping = event.isJumping();
        this.sneaking = event.isSneaking();
    }

    @Unique
    private void fixStrafeMovement() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        RotationService rotationService = RotationService.getInstance();
        Rotation rotation = rotationService.getCurrentRotation();
        RotationPlan plan = rotationService.getStoredRotationPlan();

        float z = this.movementForward;
        float x = this.movementSideways;

        if (!(plan == null || !plan.isApplyVelocityFix() || rotation == null || player == null)) {
            float deltaYaw = player.getYaw() - rotation.getYaw();

            float newX = x * MathHelper.cos(deltaYaw * 0.017453292f) - z * MathHelper.sin(deltaYaw * 0.017453292f);
            float newZ = z * MathHelper.cos(deltaYaw * 0.017453292f) + x * MathHelper.sin(deltaYaw * 0.017453292f);

            this.movementSideways = Math.round(newX);
            this.movementForward = Math.round(newZ);
        }
    }

}