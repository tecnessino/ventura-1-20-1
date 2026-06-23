package lol.ventura.foundation.rotation;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Data
@AllArgsConstructor
public class Rotation {
    public static final Rotation ZERO = new Rotation(0f, 0f);

    private float yaw, pitch;

    public Vec3d getRotationVec() {
        float yawCos = MathHelper.cos(-yaw * 0.017453292f);
        float yawSin = MathHelper.sin(-yaw * 0.017453292f);
        float pitchCos = MathHelper.cos(pitch * 0.017453292f);
        float pitchSin = MathHelper.sin(pitch * 0.017453292f);
        return new Vec3d((yawSin * pitchCos), (-pitchSin), (yawCos * pitchCos));
    }

    public Rotation gcd() {
        float gcd = RotationService.getGcd();

        Rotation rotation = RotationService.getInstance().getServerRotation();

        float deltaYaw = yaw - rotation.yaw;
        float deltaPitch = pitch - rotation.pitch;

        float g1 = Math.round(deltaYaw / gcd) * gcd;
        float g2 = Math.round(deltaPitch / gcd) * gcd;

        float newYaw = rotation.yaw + g1;
        float newPitch = rotation.pitch + g2;

        return new Rotation(newYaw, Math.min(Math.max(newPitch, -90f), 90f));
    }
}