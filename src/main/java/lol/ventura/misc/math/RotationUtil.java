package lol.ventura.misc.math;

import lol.ventura.foundation.GameAccessor;
import lombok.experimental.UtilityClass;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@UtilityClass
public class RotationUtil implements GameAccessor {
    public Vec3d closestPoint(final Vec3d eyePos, final Box box) {
        return new Vec3d(
                Math.max(box.minX, Math.min(eyePos.x, box.maxX)),
                Math.max(box.minY, Math.min(eyePos.y, box.maxY)) - (box.maxY - box.minY) / 5.43f,
                Math.max(box.minZ, Math.min(eyePos.z, box.maxZ))
        );
    }

    public Vec3d getBestAimPoint(final Box box) {
        final Vec3d eyePos = mc.player.getEyePos();
        if (box.minX < eyePos.x && eyePos.x < box.maxX &&
                box.minZ < eyePos.z && eyePos.z < box.maxZ) {
            return new Vec3d(
                    box.minX + (box.maxX - box.minX) / 2.0,
                    Math.max(box.minY, Math.min(eyePos.y, box.maxY)),
                    box.minZ + (box.maxZ - box.minZ) / 2.0
            );
        }

        return closestPoint(eyePos, box);
    }

    public Vector2f getRotations(final Vec3d from, final Vec3d to) {
        final Vec3d delta = new Vec3d(
                to.x - from.x,
                to.y - from.y,
                to.z - from.z
        );

        return getRotations(delta);
    }

    public Vector2f getRotations(final Vec3d delta) {
        return new Vector2f(
                (float) getYaw(delta),
                (float) getPitch(delta.y, delta.horizontalLength())
        );
    }

    public double getPitch(final double deltaY, final double distance) {
        return -Math.toDegrees(Math.atan2(deltaY, distance));
    }

    public double getYaw(final Vec3d delta) {
        return Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90;
    }

    public static Vec3d getDirection(Vector2f rotation) {
        float pitch = (float) Math.toRadians(rotation.getY());
        float yaw = (float) Math.toRadians(rotation.getX());

        double x = -Math.sin(yaw) * Math.cos(pitch);
        double y = -Math.sin(pitch);
        double z = Math.cos(yaw) * Math.cos(pitch);

        return new Vec3d(x, y, z);
    }

}
