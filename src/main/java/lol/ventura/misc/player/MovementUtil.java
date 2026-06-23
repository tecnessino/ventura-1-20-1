package lol.ventura.misc.player;

import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.rotation.movement.DirectionalInput;
import lombok.experimental.UtilityClass;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@UtilityClass
public class MovementUtil implements GameAccessor {
    public boolean moving() {
        return mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
    }

    public double speed() {
        return Math.hypot(mc.player.getVelocity().x, mc.player.getVelocity().z);
    }

    public void strafe(final double speed) {
        final double direction = direction(mc.player.getYaw());

        if (moving()) {
            mc.player.setVelocity(
                    -Math.sin(direction) * speed,
                    mc.player.getVelocity().y,
                    Math.cos(direction) * speed
            );
        } else {
            mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
        }
    }

    public void stop() {
        mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
    }

    public double direction(final float facingYaw) {
        float yaw = facingYaw, forward = 1f;

        if (mc.player.input.pressingBack) {
            yaw += 180f;
            forward = -0.5f;
        } else if (mc.player.input.pressingForward) {
            forward = 0.5f;
        }

        if (mc.player.input.pressingLeft) {
            yaw -= 90f * forward;
        }

        if (mc.player.input.pressingRight) {
            yaw += 90f * forward;
        }

        return Math.toRadians(yaw);
    }

    public float getDegreesRelativeToView(Vec3d positionRelativeToPlayer, float yaw) {
        float currentYaw = MathHelper.wrapDegrees(yaw);

        float optimalYaw = (float) Math.atan2(-positionRelativeToPlayer.x, positionRelativeToPlayer.z);
        currentYaw = MathHelper.wrapDegrees((float) Math.toDegrees(currentYaw));

        return MathHelper.wrapDegrees((float) Math.toDegrees(optimalYaw - currentYaw));
    }

    public DirectionalInput getDirectionalInputForDegrees(DirectionalInput directionalInput, float dgs, float deadAngle) {
        boolean forwards = directionalInput.forwards;
        boolean backwards = directionalInput.backwards;
        boolean left = directionalInput.left;
        boolean right = directionalInput.right;

        if (dgs >= -90.0F + deadAngle && dgs <= 90.0F - deadAngle) {
            forwards = true;
        } else if (dgs < -90.0 - deadAngle || dgs > 90.0 + deadAngle) {
            backwards = true;
        }

        if (dgs >= 0.0F + deadAngle && dgs <= 180.0F - deadAngle) {
            right = true;
        } else if (dgs >= -180.0F + deadAngle && dgs <= 0.0F - deadAngle) {
            left = true;
        }

        return new DirectionalInput(forwards, backwards, left, right);
    }

    public float getMovementDirectionOfInput(float facingYaw, DirectionalInput input) {
        var actualYaw = facingYaw;
        var forward = 1f;

        if (input.backwards) {
            actualYaw += 180f;
            forward = -0.5f;
        } else if (input.forwards) {
            forward = 0.5f;
        }

        if (input.left) {
            actualYaw -= 90f * forward;
        }
        if (input.right) {
            actualYaw += 90f * forward;
        }

        return actualYaw;
    }

    public void setSpeed(double speed) {
        if (!moving()) {
            mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
            return;
        }

        double yaw = direction(mc.player.getYaw());
        double x = -Math.sin(yaw) * speed;
        double z = Math.cos(yaw) * speed;

        mc.player.setVelocity(x, mc.player.getVelocity().y, z);
    }

}
