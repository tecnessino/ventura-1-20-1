package lol.ventura.features.modules.movement;

import lol.ventura.features.events.TickEvent;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lol.ventura.foundation.rotation.Rotation;
import lol.ventura.foundation.rotation.RotationService;
import lol.ventura.foundation.rotation.movement.DirectionalInput;
import lol.ventura.misc.player.MovementUtil;
import lol.ventura.misc.request.Priority;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

@ModuleDescriptor(name = "Scaffold Walk", category = Category.MOVEMENT, brief = "rusztowanie", key = GLFW.GLFW_KEY_V)
public class ScaffoldWalk extends Module {
    public ScaffoldWalk(ModuleDescriptor descriptor) {
        super(descriptor);
    }

    private BlockCache data = null;
    private boolean isOnRightSide = false;

    private final IEventListener<TickEvent> updateEvent = event -> {
        rotate();

        data = findCache((int) (mc.player.getY() - 1));

        if (data != null) {
            place();
        }
    };

    private void rotate() {
        DirectionalInput input = new DirectionalInput(mc.player.input.pressingForward, mc.player.input.pressingBack, mc.player.input.pressingLeft, mc.player.input.pressingRight);
        var direction = MovementUtil.getMovementDirectionOfInput(mc.player.yaw, input) + 180;

        var movingYaw = Math.round(direction / 45) * 45;
        var isMovingStraight = movingYaw % 90 == 0f;

        Rotation rotation;
        if (isMovingStraight) {
            rotation = getRotationForStraightInput(movingYaw);
        } else {
            rotation = getRotationForNoInput(movingYaw);
        }

        RotationService.getInstance().queue(rotation, 5f, Priority.VERY_IMPORTANT, this, true, false);
    }

    private Rotation getRotationForNoInput(float yaww) {
        double axisMovement = Math.floor(yaww / 90) * 90;
        float yaw = (float) (axisMovement + 45);
        float pitch = 75f;
        return new Rotation(yaw, pitch);
    }

    private Rotation getRotationForStraightInput(float movingYaw) {
        if (mc.player != null && mc.player.isOnGround()) {
            isOnRightSide = Math.floor(mc.player.getX() + Math.cos(Math.toRadians(movingYaw)) * 0.5) != Math.floor(mc.player.getX()) ||
                    Math.floor(mc.player.getZ() + Math.sin(Math.toRadians(movingYaw)) * 0.5) != Math.floor(mc.player.getZ());

            final BlockPos posInDirection = BlockPos.ofFloored(mc.player.pos.offset(Direction.fromRotation((double) movingYaw), 0.6));

            boolean isLeaningOffBlock = mc.world.getBlockState(mc.player.getBlockPos().down()) != null && mc.world.getBlockState(mc.player.getBlockPos().down()).isAir();
            boolean nextBlockIsAir = mc.world.getBlockState(posInDirection.down()) != null && mc.world.getBlockState(posInDirection.down()).isAir();

            if (isLeaningOffBlock && nextBlockIsAir) {
                isOnRightSide = !isOnRightSide;
            }
        }
        float finalYaw = movingYaw + (isOnRightSide ? 45 : -45);
        return new Rotation(finalYaw, 75.7f);
    }

    private void place() {
        if (data == null) {
            return;
        }

        if (!(mc.crosshairTarget instanceof BlockHitResult blockHitResult)) return;

        //XDDDDDDDDDDDDDDDDDDDDDD
        if (blockHitResult.getType() != HitResult.Type.BLOCK) {
            RotationService.getInstance().queue(RotationService.getInstance().makeRotation(hitVec(data), mc.cameraEntity.getEyePos()), 10f, Priority.VERY_IMPORTANT, this, true, false);
            if (mc.player.isOnGround()) {
                mc.player.jump();
            }

            return;
        }

        if (mc.world.getBlockState(data.getPosition()).getBlock() instanceof AirBlock) {
            return;
        }

        ActionResult actionResult2 = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHitResult);

        if (actionResult2.isAccepted()) {
            if (actionResult2.shouldSwingHand()) {
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.gameRenderer.firstPersonRenderer.resetEquipProgress(Hand.MAIN_HAND);
            }
        }
    }

    @AllArgsConstructor
    @Getter
    private class BlockCache {
        private final BlockPos position;
        private final Direction face;
    }

    private BlockCache findCache(final int positionY) {
        final BlockPos positionBelowPlayer = new BlockPos(
                mc.player.getBlockX(),
                positionY,
                mc.player.getBlockZ()
        );

        if (!(mc.world.getBlockState(positionBelowPlayer).getBlock() instanceof AirBlock)) {
            return null;
        }

        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                for (int multiplier = 1; multiplier > -3; multiplier -= 2) {
                    final BlockPos multipliedPosition = positionBelowPlayer.add(x * multiplier, 0, z * multiplier);

                    if (mc.world.getBlockState(multipliedPosition).getBlock() instanceof AirBlock) {
                        for (final Direction direction : Direction.values()) {
                            final BlockPos facingPosition = multipliedPosition.offset(direction);
                            final BlockState material = mc.world.getBlockState(facingPosition).getBlock().getDefaultState();

                            if (material.isSolid() && !material.isLiquid()) {
                                return new BlockCache(facingPosition, direction.getOpposite());
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private Vec3d hitVec(final BlockCache cache) {
        final Direction direction = cache.getFace();
        final BlockPos pos = cache.getPosition();

        Vec3d hitVec = new Vec3d(pos.getX(), pos.getY(), pos.getZ());

        switch (direction) {
            case DOWN -> hitVec = hitVec.add(0.45 + Math.random() * 0.1, 1, 0.45 + Math.random() * 0.1);
            case UP -> hitVec = hitVec.add(0.45 + Math.random() * 0.1, 0, 0.45 + Math.random() * 0.1);
            case NORTH -> hitVec = hitVec.add(0.45 + Math.random() * 0.1, 0.45 + Math.random() * 0.1, 0);
            case SOUTH -> hitVec = hitVec.add(0.45 + Math.random() * 0.1, 0.45 + Math.random() * 0.1, 1);
            case WEST -> hitVec = hitVec.add(0, 0.45 + Math.random() * 0.1, 0.45 + Math.random() * 0.1);
            case EAST -> hitVec = hitVec.add(1, 0.45 + Math.random() * 0.1, 0.45 + Math.random() * 0.1);
        }

        return hitVec;
    }
}
