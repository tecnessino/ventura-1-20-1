package lol.ventura.features.modules.movement;

import lol.ventura.features.events.TickEvent;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import net.minecraft.entity.effect.StatusEffects;
import org.lwjgl.glfw.GLFW;

@ModuleDescriptor(
        name = "Air Jump",
        category = Category.MOVEMENT,
        brief = "Allows you to jump in the air, even when not touching the ground."
)
public class AirJump extends Module {
    private int cooldown = 0;
    private boolean wasJumping = false;
    private final double baseJumpHeight = 0.42;

    public AirJump(ModuleDescriptor descriptor) {
        super(descriptor);
    }

    private final IEventListener<TickEvent> updateEvent = event -> {
        if (!isEnabled() || mc.player == null || mc.world == null) return;

        boolean jumpPressed = mc.options.jumpKey.isPressed();

        if (shouldBlockAirJump()) return;

        if (jumpPressed && canAirJump()) {
            executeAirJump();
        }

        wasJumping = jumpPressed;
        if (cooldown > 0) cooldown--;
    };

    private boolean shouldBlockAirJump() {
        return mc.player.isOnGround()
                || mc.player.isTouchingWater()
                || mc.player.isInLava()
                || mc.player.isClimbing()
                || mc.player.isFallFlying()
                || mc.player.hasVehicle()
                || mc.player.isSpectator()
                || mc.player.hasStatusEffect(StatusEffects.LEVITATION);
    }

    private boolean canAirJump() {
        return !mc.player.isOnGround()
                && cooldown <= 0
                && !wasJumping;
    }

    private void executeAirJump() {
        double jumpVelocity = baseJumpHeight;
        if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            int amplifier = mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier();
            jumpVelocity += (amplifier + 1) * 0.1;
        }

        mc.player.setVelocity(mc.player.getVelocity().x, jumpVelocity, mc.player.getVelocity().z);
        mc.player.velocityDirty = true;
        cooldown = 5;
    }
}