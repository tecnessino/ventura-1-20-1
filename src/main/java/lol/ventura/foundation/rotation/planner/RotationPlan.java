package lol.ventura.foundation.rotation.planner;

import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.rotation.Rotation;
import lombok.Data;

@Data
public class RotationPlan implements GameAccessor {
    private Rotation rotation;
    private Float baseTurnSpeed;
    private int ticksUntilReset;
    private float resetThreshold;
    private boolean considerInventory, applyVelocityFix, changeLook;

    private final AngleSmooth angleSmooth;

    public RotationPlan(Rotation rotation, Float baseTurnSpeed, int ticksUntilReset,
                        float resetThreshold, boolean considerInventory, boolean applyVelocityFix, boolean changeLook) {
        this.rotation = rotation;
        this.baseTurnSpeed = baseTurnSpeed;
        this.ticksUntilReset = ticksUntilReset;
        this.resetThreshold = resetThreshold;
        this.considerInventory = considerInventory;
        this.applyVelocityFix = applyVelocityFix;
        this.changeLook = changeLook;

        this.angleSmooth = new AngleSmooth(baseTurnSpeed);
    }

    public Rotation nextRotation(Rotation fromRotation, boolean isResetting) {
        if (isResetting) {
            return angleSmooth.limitAngleChange(fromRotation, new Rotation(mc.player.getYaw(), mc.player.getPitch()));
        }

        return angleSmooth.limitAngleChange(fromRotation, rotation);
    }
}