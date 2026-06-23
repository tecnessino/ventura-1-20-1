package lol.ventura.misc.player;

import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.rotation.Rotation;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

@UtilityClass
public class RaytracingExtensions implements GameAccessor {
    public static BlockHitResult raycast(double range, Rotation rotation, boolean includeFluids) {
        Entity entity = mc.cameraEntity;

        if (entity == null) {
            return null;
        }

        Vec3d start = entity.getCameraPosVec(1.0F);
        Vec3d rotationVec = rotation.getRotationVec();
        Vec3d end = start.add(rotationVec.x * range, rotationVec.y * range, rotationVec.z * range);

        World world = mc.world;
        if (world == null) {
            return null;
        }

        RaycastContext.FluidHandling fluidHandling = includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE;
        RaycastContext context = new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, fluidHandling, entity);

        return world.raycast(context);
    }
}