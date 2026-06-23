package lol.ventura.foundation.rotation;

import lol.ventura.features.events.GameMoveInputEvent;
import lol.ventura.features.events.NetworkPacketEvent;
import lol.ventura.features.events.PlayerVelocityStrafeEvent;
import lol.ventura.features.events.SimulationTickEvent;
import lol.ventura.features.modules.combat.BackTrack;
import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.Service;
import lol.ventura.foundation.event.EventBus;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.prediction.MovementPrediction;
import lol.ventura.foundation.prediction.MovementPredictionImpl;
import lol.ventura.foundation.prediction.MovementPredictionInput;
import lol.ventura.foundation.rotation.planner.RotationPlan;
import lol.ventura.misc.request.Priority;
import lol.ventura.misc.request.RequestHandler;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Getter
public class RotationService extends Service implements GameAccessor {
    @Getter
    @Setter
    private static RotationService instance;

    public RotationService() {
        EventBus.getInstance().register(this);
    }

    private RotationPlan prevRotationPlan;

    private final RequestHandler<RotationPlan> rotationPlanHandler = new RequestHandler<>();

    private Rotation currentRotation, prevRotation;

    private Rotation theoreticalServerRotation = Rotation.ZERO, actualServerRotation = Rotation.ZERO;

    @Setter
    private Pair<Float, Float> rotationPitch = new Pair<>(0f, 0f);

    public void queue(Rotation rotation, Float speed, int ticks, float resetThreshold, Priority priority, Module provider, boolean fixVelocity, boolean changeLook) {
        queue(new RotationPlan(rotation, speed, ticks, resetThreshold, false, fixVelocity, changeLook), priority, provider);
    }

    public void queue(Rotation rotation, Float speed, Priority priority, Module provider, boolean fixVelocity, boolean changeLook) {
        queue(new RotationPlan(rotation, speed, 3, 3, false, fixVelocity, changeLook), priority, provider);
    }

    private void queue(RotationPlan plan, Priority priority, Module provider) {
        rotationPlanHandler.request(new RequestHandler.Request<>(
                plan.isChangeLook() ? 0 : plan.getTicksUntilReset(),
                priority.getPriority(),
                provider,
                plan
        ));
    }

    private final IEventListener<PlayerVelocityStrafeEvent> onVelocity = e -> {
        if (getStoredRotationPlan() != null && getStoredRotationPlan().isApplyVelocityFix()) {
            e.setVelocity(fixVelocity(e.getVelocity(), e.getMovementInput(), e.getSpeed()));
        }
    };

    private final IEventListener<NetworkPacketEvent> onPacket = e -> {
        var packet = e.getPacket();

        Rotation rotation;
        if (packet instanceof PlayerMoveC2SPacket && ((PlayerMoveC2SPacket) packet).changeLook) {
            rotation = new Rotation(((PlayerMoveC2SPacket) packet).yaw, ((PlayerMoveC2SPacket) packet).pitch);
        } else if (packet instanceof PlayerPositionLookS2CPacket) {
            rotation = new Rotation(((PlayerPositionLookS2CPacket) packet).getYaw(), ((PlayerPositionLookS2CPacket) packet).getPitch());
        } else {
            return;
        }

        if (!e.isCancelled()) {
            actualServerRotation = rotation;
        }

        theoreticalServerRotation = rotation;
    };

    private final IEventListener<GameMoveInputEvent> onMove = e -> {
        MovementPredictionInput input = MovementPredictionInput.client(e.getDirectionalInput());

        input.sneaking = e.isSneaking();
        input.jumping = e.isJumping();

        MovementPrediction simulatedPlayer = MovementPredictionImpl.client(input);

        simulatedPlayer.tick();

        Vec3d oldPos = mc.player.getPos();
        mc.player.setPosition(simulatedPlayer.getPos());

        EventBus.getInstance().emit(new SimulationTickEvent());
        update();

        mc.player.setPosition(oldPos);
    };

    private void update() {
        RotationPlan storedRotationPlan = getStoredRotationPlan();
        if (storedRotationPlan == null) {
            return;
        }

        Rotation playerRotation = new Rotation(mc.player.yaw, mc.player.pitch);

        if (getRotationPlan() == null) {
            double difference = rotationDifference(getServerRotation(), playerRotation);

            if (difference < storedRotationPlan.getResetThreshold() || storedRotationPlan.isChangeLook()) {
                if (currentRotation != null) {
                    mc.player.yaw = currentRotation.getYaw() + angleDifference(mc.player.yaw, currentRotation.getYaw());
                    mc.player.renderYaw = mc.player.yaw;
                    mc.player.lastRenderYaw = mc.player.yaw;
                }

                currentRotation = null;
                prevRotationPlan = null;
                return;
            }
        }

        Rotation rotation = storedRotationPlan
                .nextRotation(currentRotation != null ? currentRotation : playerRotation, getRotationPlan() == null)
                .gcd();

        setCurrentRotation(rotation);
        prevRotationPlan = storedRotationPlan;

        if (storedRotationPlan.isChangeLook()) {
            mc.player.prevPitch = mc.player.pitch;
            mc.player.prevYaw = mc.player.yaw;

            mc.player.yaw = rotation.getYaw();
            mc.player.pitch = rotation.getPitch();
        }

        rotationPlanHandler.tick(1);
    }

    public Rotation displayRotations() {
        return getCurrentRotation() == null ? getServerRotation() : getCurrentRotation();
    }

    public RotationPlan getStoredRotationPlan() {
        return getRotationPlan() != null ? getRotationPlan() : prevRotationPlan;
    }

    public Rotation makeRotation(Vec3d vec, Vec3d eyes) {
        double diffX = vec.x - eyes.x;
        double diffY = vec.y - eyes.y;
        double diffZ = vec.z - eyes.z;

        return new Rotation(
                MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90f),
                MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ))))
        );
    }

    public static float getGcd() {
        float f = mc.options.getMouseSensitivity().getValue().floatValue() * 0.6f + 0.2f;
        return f * f * f * 8.0f * 0.15f;
    }

    public double rotationDifference(Rotation a, Rotation b) {
        return Math.hypot(Math.abs(angleDifference(a.getYaw(), b.getYaw())),
                Math.abs((a.getPitch() - b.getPitch())));
    }

    public float angleDifference(float a, float b) {
        return MathHelper.wrapDegrees(a - b);
    }

    private RotationPlan getRotationPlan() {
        return rotationPlanHandler.getActiveRequestValue();
    }

    private boolean isLagging() {
        return BackTrack.isLagging();
    }

    public void setCurrentRotation(Rotation value) {
        if (value != null) {
            prevRotation = (currentRotation != null) ? currentRotation : new Rotation(mc.player.yaw, mc.player.pitch);
        }

        currentRotation = value;
    }

    public Rotation getServerRotation() {
        return isLagging() ? theoreticalServerRotation : actualServerRotation;
    }

    private Vec3d fixVelocity(Vec3d currVelocity, Vec3d movementInput, float speed) {
        if (currentRotation != null) {
            Rotation rotation = getCurrentRotation();
            if (rotation != null) {
                float yaw = rotation.getYaw();
                double d = movementInput.lengthSquared();

                if (d < 1.0E-7) {
                    return Vec3d.ZERO;
                } else {
                    Vec3d vec3d = (d > 1.0) ? movementInput.normalize().multiply(speed) : movementInput.multiply(speed);

                    double f = MathHelper.sin(yaw * 0.017453292f);
                    double g = MathHelper.cos(yaw * 0.017453292f);

                    return new Vec3d(
                            vec3d.x * g - vec3d.z * f,
                            vec3d.y,
                            vec3d.z * g + vec3d.x * f
                    );
                }
            }
        }

        return currVelocity;
    }
}