package lol.ventura.foundation.prediction;

import lol.ventura.foundation.rotation.movement.DirectionalInput;
import lol.ventura.misc.player.MovementUtil;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Setter
public class MovementPredictionInput extends Input {
    public boolean sprinting;

    public MovementPredictionInput(DirectionalInput directionalInput, boolean jumping, boolean sprinting, boolean sneaking) {
        this.pressingForward = directionalInput.forwards;
        this.pressingBack = directionalInput.backwards;
        this.pressingLeft = directionalInput.left;
        this.pressingRight = directionalInput.right;

        this.jumping = jumping;
        this.sprinting = sprinting;
        this.sneaking = sneaking;
    }

    public void update() {
        if (this.pressingForward != this.pressingBack) {
            this.movementForward = this.pressingForward ? 1.0f : -1.0f;
        } else {
            this.movementForward = 0.0f;
        }

        this.movementSideways = (pressingLeft == pressingRight) ? 0.0f : (pressingLeft ? 1.0f : -1.0f);

        if (this.sneaking) {
            this.movementSideways *= 0.3f;
            this.movementForward *= 0.3f;
        }
    }

    public static final double MAX_WALKING_SPEED = 0.121;

    public static MovementPredictionInput client(DirectionalInput directionalInput) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        MovementPredictionInput input = new MovementPredictionInput(
                directionalInput,
                player.input.jumping,
                player.isSprinting(),
                player.isSneaking()
        );

        return input;
    }

    public static MovementPredictionInput guessInput(ClientPlayerEntity entity) {
        Vec3d velocity = entity.getPos().subtract(new Vec3d(entity.prevX, entity.prevY, entity.prevZ));

        double horizontalVelocity = velocity.horizontalLengthSquared();

        boolean sprinting = horizontalVelocity >= MAX_WALKING_SPEED * MAX_WALKING_SPEED;

        DirectionalInput input = DirectionalInput.NONE;

        if (horizontalVelocity > 0.05 * 0.05) {
            float velocityAngle = MovementUtil.getDegreesRelativeToView(velocity, entity.getYaw());

            float velocityAngle1 = MathHelper.wrapDegrees(velocityAngle);

            input = MovementUtil.getDirectionalInputForDegrees(DirectionalInput.NONE, velocityAngle1, 20.0f);
        }

        boolean jumping = !entity.isOnGround();

        return new MovementPredictionInput(
                input,
                jumping,
                sprinting,
                entity.isSneaking()
        );
    }
}