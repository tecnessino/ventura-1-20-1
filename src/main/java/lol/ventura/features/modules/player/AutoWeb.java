package lol.ventura.features.modules.player;

import lol.ventura.features.combat.CombatService;
import lol.ventura.features.events.TickEvent;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lol.ventura.foundation.rotation.Rotation;
import lol.ventura.foundation.rotation.RotationService;
import lol.ventura.misc.math.Stopwatch;
import lol.ventura.misc.player.InventoryUtil;
import lol.ventura.misc.request.Priority;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;

@ModuleDescriptor(name = "AutoWeb", category = Category.PLAYER, brief = "lapie vctima w pulapke")
public class AutoWeb extends Module {

    public AutoWeb(ModuleDescriptor descriptor) {
        super(descriptor);
    }

    private final Stopwatch timer = new Stopwatch();

    private final IEventListener<TickEvent> onTick = event -> {
        if (mc.player == null || mc.interactionManager == null || mc.world == null) return;

        Entity target = CombatService.getInstance().getTargets()
                .stream()
                .min(Comparator.comparingDouble(entity -> entity.squaredDistanceTo(mc.player)))
                .orElse(null);

        if (target == null) return;

        BlockPos basePos = target.getBlockPos();

        BlockPos[] positions = new BlockPos[]{
                basePos,
                basePos.up(),
                basePos.north(),
                basePos.south(),
                basePos.east(),
                basePos.west()
        };

        if (!timer.elapsed(250)) return;

        var result = InventoryUtil.findItemInHotBar(Items.COBWEB);
        if (!result.found()) return;

        ClientPlayerEntity player = mc.player;
        int oldSlot = player.getInventory().selectedSlot;
        player.getInventory().selectedSlot = result.slot();

        for (BlockPos targetPos : positions) {
            if (canPlace(targetPos)) {
                for (Direction dir : Direction.values()) {
                    BlockPos blockPos = targetPos.offset(dir);
                    if (mc.world.getBlockState(blockPos).isOpaque()) {
                        Vec3d hitVec = getPreciseHitVec(blockPos, dir.getOpposite());
                        float[] rots = rotation(hitVec);

                        RotationService.getInstance().queue(
                                new Rotation(rots[0], rots[1]),
                                180f,
                                Priority.IMPORTANT,
                                this,
                                true,
                                false
                        );

                        BlockHitResult hit = new BlockHitResult(hitVec, dir.getOpposite(), blockPos, false);
                        ActionResult interactResult = mc.interactionManager.interactBlock(player, Hand.MAIN_HAND, hit);

                        if (interactResult.isAccepted()) {
                            player.swingHand(Hand.MAIN_HAND);
                            timer.reset();
                            break;
                        }
                    }
                }
                break;
            }
        }

        player.getInventory().selectedSlot = oldSlot;
    };

    private boolean canPlace(BlockPos pos) {
        return mc.world.getBlockState(pos).isReplaceable()
                && mc.world.getBlockState(pos.down()).isOpaque();
    }

    private float[] rotation(Vec3d target) {
        Vec3d eyes = mc.player.getEyePos();
        double dx = target.x - eyes.x;
        double dy = target.y - eyes.y;
        double dz = target.z - eyes.z;

        double distXZ = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distXZ));

        yaw = MathHelper.wrapDegrees(yaw);
        pitch = MathHelper.clamp(pitch, -90F, 90F);

        return new float[]{yaw, pitch};
    }

    private Vec3d getPreciseHitVec(BlockPos blockPos, Direction face) {
        return Vec3d.ofCenter(blockPos).add(Vec3d.of(face.getVector()).multiply(0.5));
    }
}
