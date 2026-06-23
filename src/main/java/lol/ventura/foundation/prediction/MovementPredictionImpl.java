package lol.ventura.foundation.prediction;

import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import lol.ventura.foundation.GameAccessor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.util.HashSet;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.max;

@Getter @Setter
public class MovementPredictionImpl implements MovementPrediction, GameAccessor {
    private final PlayerEntity player;
    private MovementPredictionInput input;

    private Vec3d pos, velocity;

    private Box boundingBox;

    private final float yaw, pitch;

    private boolean sprinting, isJumping, isFallFlying, onGround, horizontalCollision, verticalCollision,
            touchingWater, isSwimming, submergedInWater;

    private float fallDistance;
    private int jumpingCooldown;

    private final Object2DoubleArrayMap<TagKey<Fluid>> fluidHeight;
    private final HashSet<TagKey<Fluid>> submergedFluidTag;

    public MovementPredictionImpl(PlayerEntity player,
                                  MovementPredictionInput input,
                                  Vec3d pos, Vec3d velocity, Box boundingBox,
                                  float yaw, float pitch,
                                  boolean sprinting,
                                  float fallDistance,
                                  int jumpingCooldown,
                                  boolean isJumping,
                                  boolean isFallFlying,
                                  boolean onGround,
                                  boolean horizontalCollision, boolean verticalCollision,
                                  boolean touchingWater, boolean isSwimming,
                                  boolean submergedInWater,
                                  Object2DoubleArrayMap<TagKey<Fluid>> fluidHeight,
                                  HashSet<TagKey<Fluid>> submergedFluidTag) {
        this.player = player;
        this.input = input;
        this.pos = pos;
        this.velocity = velocity;
        this.boundingBox = boundingBox;
        this.yaw = yaw;
        this.pitch = pitch;
        this.sprinting = sprinting;
        this.fallDistance = fallDistance;
        this.jumpingCooldown = jumpingCooldown;
        this.isJumping = isJumping;
        this.isFallFlying = isFallFlying;
        this.onGround = onGround;
        this.horizontalCollision = horizontalCollision;
        this.verticalCollision = verticalCollision;
        this.touchingWater = touchingWater;
        this.isSwimming = isSwimming;
        this.submergedInWater = submergedInWater;
        this.fluidHeight = fluidHeight;
        this.submergedFluidTag = submergedFluidTag;
    }

    public static MovementPredictionImpl client(MovementPredictionInput input) {
        PlayerEntity player = mc.player;
        return new MovementPredictionImpl(
                player,
                input,
                player.getPos(),
                player.getVelocity(),
                player.getBoundingBox(),
                player.getYaw(),
                player.getPitch(),
                player.isSprinting(),
                player.fallDistance,
                player.jumpingCooldown,
                player.jumping,
                player.isFallFlying(),
                player.isOnGround(),
                player.horizontalCollision,
                player.verticalCollision,
                player.isTouchingWater(),
                player.isSwimming(),
                player.isSubmergedInWater(),
                new Object2DoubleArrayMap<>(player.fluidHeight),
                new HashSet<>(player.submergedFluidTag)
        );
    }

    public static MovementPredictionImpl fromOtherPlayer(PlayerEntity player, MovementPredictionInput input) {
        return new MovementPredictionImpl(
                player,
                input,
                player.getPos(),
                player.getPos().subtract(new Vec3d(player.prevX, player.prevY, player.prevZ)),
                player.getBoundingBox(),
                player.getYaw(),
                player.getPitch(),
                player.isSprinting(),
                player.fallDistance,
                player.jumpingCooldown,
                player.jumping,
                player.isFallFlying(),
                player.isOnGround(),
                player.horizontalCollision,
                player.verticalCollision,
                player.isTouchingWater(),
                player.isSwimming(),
                player.isSubmergedInWater(),
                new Object2DoubleArrayMap<>(player.fluidHeight),
                new HashSet<>(player.submergedFluidTag)
        );
    }

    @Override
    public Vec3d getPos() {
        return pos;
    }

    @Override
    public void tick() {
        // Ignore because world limit is -65
        if (pos.y <= -70) {
            return;
        }

        this.input.update();

        checkWaterState();
        updateSubmergedInWaterState();
        updateSwimming();

        // LivingEntity.tickMovement()
        if (this.jumpingCooldown > 0) {
            --this.jumpingCooldown;
        }

        this.isJumping = this.input.jumping;

        Vec3d vec3d = this.getVelocity();
        double d = vec3d.x;
        double e = vec3d.y;
        double f = vec3d.z;
        if (abs(vec3d.x) < 0.003) {
            d = 0.0;
        }

        if (abs(vec3d.y) < 0.003) {
            e = 0.0;
        }

        if (abs(vec3d.z) < 0.003) {
            f = 0.0;
        }

        this.velocity = new Vec3d(d, e, f);

        if (this.isJumping) {
            double g;
            if (this.isInLava()) {
                g = this.getFluidHeight(FluidTags.LAVA);
            } else {
                g = this.getFluidHeight(FluidTags.WATER);
            }

            boolean bl = this.isTouchingWater() && g > 0.0;
            double h = this.getSwimHeight();

            if (bl && (!this.onGround || g > h)) {
                this.swimUpward();
            } else if (this.isInLava() && (!this.onGround || g > h)) {
                this.swimUpward();
            } else if ((this.onGround || bl && g <= h) && jumpingCooldown == 0) {
                this.jump();
                this.jumpingCooldown = 10;
            }
        }

        double sidewaysSpeed = input.movementSideways * 0.98;
        double forwardSpeed = input.movementForward * 0.98;
        double upwardsSpeed = 0.0;

        Vec3d vec3d2 = new Vec3d(sidewaysSpeed, upwardsSpeed, forwardSpeed);
        if (this.player.hasStatusEffect(StatusEffects.SLOW_FALLING) || this.player.hasStatusEffect(StatusEffects.LEVITATION)) {
            this.onLanding();
        }

        this.travel(vec3d2);
    }

    private void travel(Vec3d movementInput) {
        if (this.isSwimming() && !this.player.hasVehicle()) {
            double d = this.getRotationVector().y;
            double e = d < -0.2 ? 0.085 : 0.06;
            if (d <= 0.0 || this.input.jumping || !this.player.getWorld().getBlockState(BlockPos.ofFloored(this.pos.x, this.pos.y + 1.0 - 0.1, this.pos.z)).getFluidState().isEmpty()) {
                Vec3d vec3d = this.getVelocity();
                this.setVelocity(vec3d.add(0.0, (d - vec3d.y) * e, 0.0));
            }
        }

        double beforeTravelVelocityY = this.getVelocity().y;

        double d = 0.08;
        boolean bl = this.getVelocity().y <= 0.0;
        if (bl && this.player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            d = 0.01;
            this.onLanding();
        }

        if (this.isTouchingWater() && this.player.shouldSwimInFluids()) {
            double e = this.pos.y;
            double f = this.isSprinting() ? 0.9F : 0.8F;
            float g = 0.02F;
            float h = (float) EnchantmentHelper.getDepthStrider(this.player);
            if (h > 3.0F) {
                h = 3.0F;
            }

            if (!this.onGround) {
                h *= 0.5F;
            }

            if (h > 0.0F) {
                f += (0.54600006F - f) * h / 3.0F;
                g += (this.getMovementSpeed() - g) * h / 3.0F;
            }

            if (this.player.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
                f = 0.96F;
            }

            this.updateVelocity(g, movementInput);
            this.move(velocity);

            Vec3d vec3d = this.getVelocity();
            if (this.horizontalCollision && this.isClimbing()) {
                vec3d = new Vec3d(vec3d.x, 0.2, vec3d.z);
            }

            this.setVelocity(vec3d.multiply((double)f, 0.800000011920929, (double)f));
            Vec3d vec3d2 = this.player.applyFluidMovingSpeed(d, bl, this.getVelocity());
            this.setVelocity(vec3d2);
            if (this.horizontalCollision && this.doesNotCollide(vec3d2.x, vec3d2.y + 0.6000000238418579 - this.pos.getY() + e, vec3d2.z)) {
                this.setVelocity(new Vec3d(vec3d2.x, 0.30000001192092896, vec3d2.z));
            }
        } else if (this.isInLava() && this.player.shouldSwimInFluids() /*&& !this.canWalkOnFluid(fluidState) */) {
            double e = this.pos.y;
            this.updateVelocity(0.02F, movementInput);
            this.move(this.getVelocity());
            Vec3d vec3d3;
            if (this.getFluidHeight(FluidTags.LAVA) <= this.getSwimHeight()) {
                this.setVelocity(this.getVelocity().multiply(0.5, 0.800000011920929, 0.5));
                vec3d3 = this.player.applyFluidMovingSpeed(d, bl, this.getVelocity());
                this.setVelocity(vec3d3);
            } else {
                this.setVelocity(this.getVelocity().multiply(0.5));
            }

            if (!this.player.hasNoGravity()) {
                this.setVelocity(this.getVelocity().add(0.0, -d / 4.0, 0.0));
            }

            vec3d3 = this.getVelocity();
            if (this.horizontalCollision && this.doesNotCollide(vec3d3.x, vec3d3.y + 0.6000000238418579 - this.pos.y + e, vec3d3.z)) {
                this.setVelocity(new Vec3d(vec3d3.x, 0.30000001192092896, vec3d3.z));
            }
        } else if (this.isFallFlying) {
            double k;
            Vec3d e = this.velocity;

            if (e.y > -0.5) {
                fallDistance = 1.0f;
            }

            Vec3d vec3d3 = this.getRotationVector();
            float f = (float) (this.pitch * (Math.PI / 180));
            double g = Math.sqrt(vec3d3.x * vec3d3.x + vec3d3.z * vec3d3.z);
            double vec3d = e.horizontalLength();
            double i = vec3d3.length();
            double j = MathHelper.cos(f);
            j = j * Math.min(j * 1.0, i / 0.4);

            e = this.velocity.add(0.0, d * (-1.0 + j * 0.75), 0.0);

            if (e.y < 0.0 && g > 0.0) {
                k = e.y * -0.1 * j;
                e = e.add(vec3d3.x * k / g, k, vec3d3.z * k / g);
            }

            if (f < 0.0 && g > 0.0) {
                k = vec3d * (-MathHelper.sin(f)) * 0.04;
                e = e.add(-vec3d3.x * k / g, k * 3.2, -vec3d3.z * k / g);
            }

            if (g > 0.0) {
                e = e.add((vec3d3.x / g * vec3d - e.x) * 0.1, 0.0, (vec3d3.z / g * vec3d - e.z) * 0.1);
            }

            this.setVelocity(e.multiply(0.99, 0.98, 0.99));
            move(this.velocity);
        } else {
            BlockPos blockPos = this.player.getVelocityAffectingPos();
            float p = this.player.getWorld().getBlockState(blockPos).getBlock().getSlipperiness();
            float f = this.onGround ? p * 0.91F : 0.91F;
            Vec3d vec3d6 = this.applyMovementInput(movementInput, p);
            double q = vec3d6.y;
            if (this.player.hasStatusEffect(StatusEffects.LEVITATION)) {
                q += (0.05 * (double)(this.player.getStatusEffect(StatusEffects.LEVITATION).getAmplifier() + 1) - vec3d6.y) * 0.2;
            } else if (this.player.getWorld().isClient && !this.player.getWorld().isChunkLoaded(blockPos)) {
                if (this.pos.getY() > (double)this.player.getWorld().getBottomY()) {
                    q = -0.1;
                } else {
                    q = 0.0;
                }
            } else if (!this.player.hasNoGravity()) {
                q -= d;
            }

            if (this.player.hasNoDrag()) {
                this.setVelocity(new Vec3d(vec3d6.x, q, vec3d6.z));
            } else {
                this.setVelocity(new Vec3d(vec3d6.x * (double)f, q * 0.9800000190734863, vec3d6.z * (double)f));
            }
        }

        if (player.getAbilities().flying && !this.player.hasVehicle()) {
            velocity = new Vec3d(velocity.x, beforeTravelVelocityY * 0.6, velocity.z);
            this.onLanding();
        }
    }

    private boolean doesNotCollide(double offsetX, double offsetY, double offsetZ) {
        return this.doesNotCollide(this.getBoundingBox().offset(offsetX, offsetY, offsetZ));
    }

    private boolean doesNotCollide(Box box) {
        return this.player.getWorld().isSpaceEmpty(this.player, box) && !this.player.getWorld().containsFluid(box);
    }

    public Vec3d applyMovementInput(Vec3d movementInput, float slipperiness) {
        this.updateVelocity(this.getMovementSpeed(slipperiness), movementInput);
        this.setVelocity(this.applyClimbingSpeed(this.getVelocity()));
        this.move(this.getVelocity());
        Vec3d vec3d = this.getVelocity();
        if ((this.horizontalCollision || this.isJumping) && (this.isClimbing() || this.player.getBlockStateAtPos().isOf(Blocks.POWDER_SNOW) && PowderSnowBlock.canWalkOnPowderSnow(this.player))) {
            vec3d = new Vec3d(vec3d.x, 0.2, vec3d.z);
        }

        return vec3d;
    }

    private Vec3d applyClimbingSpeed(Vec3d motion) {
        if (this.isClimbing()) {
            this.onLanding();
            float f = 0.15F;
            double d = MathHelper.clamp(motion.x, -0.15000000596046448, 0.15000000596046448);
            double e = MathHelper.clamp(motion.z, -0.15000000596046448, 0.15000000596046448);
            double g = Math.max(motion.y, -0.15000000596046448);
            if (g < 0.0 && !this.player.getBlockStateAtPos().isOf(Blocks.SCAFFOLDING) && this.player.isHoldingOntoLadder() && this.player instanceof PlayerEntity) {
                g = 0.0;
            }

            motion = new Vec3d(d, g, e);
        }

        return motion;
    }

    private boolean isClimbing() {
        BlockPos blockPos = this.player.getBlockPos();
        BlockState blockState = this.player.getBlockStateAtPos();
        if (blockState.isIn(BlockTags.CLIMBABLE)) {
            return true;
        } else if (blockState.getBlock() instanceof TrapdoorBlock && this.canEnterTrapdoor(blockPos, blockState)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean canEnterTrapdoor(BlockPos pos, BlockState state) {
        if (state.get(TrapdoorBlock.OPEN)) {
            BlockState blockState = this.player.getWorld().getBlockState(pos.down());
            if (blockState.isOf(Blocks.LADDER) && blockState.get(LadderBlock.FACING) == state.get(TrapdoorBlock.FACING)) {
                return true;
            }
        }

        return false;
    }

    private void move(Vec3d input) {
        Vec3d movement = this.adjustMovementForSneaking(input);
        Vec3d adjustedMovement = this.adjustMovementForCollisions(movement);

        if (adjustedMovement.lengthSquared() > 1.0E-7) {
            this.pos = this.pos.add(adjustedMovement);
            this.boundingBox = this.player.getDimensions(player.getPose()).getBoxAt(this.pos);
        }

        boolean xCollision = !MathHelper.approximatelyEquals(movement.x, adjustedMovement.x);
        boolean zCollision = !MathHelper.approximatelyEquals(movement.z, adjustedMovement.z);

        this.horizontalCollision = xCollision || zCollision;
        this.verticalCollision = movement.y != adjustedMovement.y;

        onGround = verticalCollision && movement.y < 0.0;

        if (!isTouchingWater()) {
            checkWaterState();
        }

        if (onGround) {
            onLanding();
        } else if (movement.y < 0) {
            fallDistance -= (float) movement.y;
        }

        Vec3d vec3d2 = this.velocity;
        if (horizontalCollision || verticalCollision) {
            this.velocity = new Vec3d(
                    xCollision ? 0.0 : vec3d2.x,
                    onGround ? 0.0 : vec3d2.y,
                    zCollision ? 0.0 : vec3d2.z
            );
        }
    }

    private boolean method_30263() {
        return onGround || (this.fallDistance < 0.5 && !player.getWorld().isSpaceEmpty(player, boundingBox.offset(0.0, this.fallDistance - 0.5, 0.0)));
    }

    private Vec3d adjustMovementForSneaking(Vec3d movement) {
        boolean isSelfMovement = true; // (type == MovementType.SELF || type == MovementType.PLAYER)
        boolean isFlying = false; // abilities.isFlying

        if (!isFlying && movement.y <= 0.0 && isSelfMovement && this.shouldClipAtLedge() && this.method_30263()) {
            double d = movement.x;
            double e = movement.z;
            double f = 0.05;

            while (d != 0.0 && player.getWorld().isSpaceEmpty(player, boundingBox.offset(d, -0.5, 0.0))) {
                if (d < 0.05 && d >= -0.05) {
                    d = 0.0;
                    continue;
                }
                if (d > 0.0) {
                    d -= 0.05;
                    continue;
                }
                d += 0.05;
            }

            while (e != 0.0 && player.getWorld().isSpaceEmpty(player, boundingBox.offset(0.0, -0.5, e))) {
                if (e < 0.05 && e >= -0.05) {
                    e = 0.0;
                    continue;
                }
                if (e > 0.0) {
                    e -= 0.05;
                    continue;
                }
                e += 0.05;
            }

            while (d != 0.0 && e != 0.0 && player.getWorld().isSpaceEmpty(player, boundingBox.offset(d, -0.5, e))) {
                d = (d < 0.05 && d >= -0.05) ? 0.0 : (d > 0.0 ? (d - 0.05) : (d + 0.05));

                if (e < 0.05 && e >= -0.05) {
                    e = 0.0;
                    continue;
                }
                if (e > 0.0) {
                    e -= 0.05;
                    continue;
                }
                e += 0.05;
            }

            movement = new Vec3d(d, movement.y, e);
        }

        return movement;
    }

    protected boolean shouldClipAtLedge() {
        return this.input.sneaking;
    }

    private Vec3d adjustMovementForCollisions(Vec3d movement) {
        Box box = this.getBoundingBox();
        List<VoxelShape> list = this.player.getWorld().getEntityCollisions(this.player, box.stretch(movement));
        Vec3d vec3d = movement.lengthSquared() == 0.0 ? movement : Entity.adjustMovementForCollisions(this.player, movement, box, this.player.getWorld(), list);
        boolean bl = movement.x != vec3d.x;
        boolean bl2 = movement.y != vec3d.y;
        boolean bl3 = movement.z != vec3d.z;
        boolean bl4 = this.onGround || bl2 && movement.y < 0.0;
        if (this.player.getStepHeight() > 0.0F && bl4 && (bl || bl3)) {
            Vec3d vec3d2 = Entity.adjustMovementForCollisions(this.player, new Vec3d(movement.x, this.player.getStepHeight(), movement.z), box, this.player.getWorld(), list);
            Vec3d vec3d3 = Entity.adjustMovementForCollisions(this.player, new Vec3d(0.0, this.player.getStepHeight(), 0.0), box.stretch(movement.x, 0.0, movement.z), this.player.getWorld(), list);
            if (vec3d3.y < (double)this.player.getStepHeight()) {
                Vec3d vec3d4 = Entity.adjustMovementForCollisions(this.player, new Vec3d(movement.x, 0.0, movement.z), box.offset(vec3d3), this.player.getWorld(), list).add(vec3d3);
                if (vec3d4.horizontalLengthSquared() > vec3d2.horizontalLengthSquared()) {
                    vec3d2 = vec3d4;
                }
            }

            if (vec3d2.horizontalLengthSquared() > vec3d.horizontalLengthSquared()) {
                return vec3d2.add(Entity.adjustMovementForCollisions(this.player, new Vec3d(0.0, -vec3d2.y + movement.y, 0.0), box.offset(vec3d2), this.player.getWorld(), list));
            }
        }

        return vec3d;
    }

    public void updateVelocity(float speed, Vec3d movementInput) {
        Vec3d vec3d = Entity.movementInputToVelocity(movementInput, speed, this.yaw);
        this.setVelocity(this.getVelocity().add(vec3d));
    }

    private float getMovementSpeed() {
        return 0.10000000149011612f;
    }

    private float getMovementSpeed(float slipperiness) {
        if (this.onGround) {
            return getMovementSpeed() * (0.21600002f / (slipperiness * slipperiness * slipperiness));
        } else {
            return this.getAirStrafingSpeed();
        }
    }

    private float getAirStrafingSpeed() {
        float speed = 0.02f;

        if (this.input.sprinting) {
            return (speed + 0.005999999865889549f);
        }

        return speed;
    }

    private Vec3d getRotationVector() {
        return this.getRotationVector(this.pitch, this.yaw);
    }

    protected final Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;

        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);

        return new Vec3d(i * j, -k, h * j);
    }

    private float getJumpVelocity() {
        return 0.42F * this.getJumpVelocityMultiplier() + this.getJumpBoostVelocityModifier();
    }

    protected float getJumpVelocityMultiplier() {
        float f = this.player.getWorld().getBlockState(this.player.getBlockPos()).getBlock().getJumpVelocityMultiplier();
        float g = this.player.getWorld().getBlockState(this.player.getVelocityAffectingPos()).getBlock().getJumpVelocityMultiplier();
        return (double)f == 1.0 ? g : f;
    }

    public float getJumpBoostVelocityModifier() {
        return this.player.hasStatusEffect(StatusEffects.JUMP_BOOST) ? 0.1F * ((float)this.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1.0F) : 0.0F;
    }

    private void jump() {
        Vec3d vec3d = this.getVelocity();
        this.setVelocity(new Vec3d(vec3d.x, this.getJumpVelocity(), vec3d.z));
        if (this.isSprinting()) {
            float f = this.player.getYaw() * 0.017453292F;
            this.setVelocity(this.getVelocity().add(-MathHelper.sin(f) * 0.2F, 0.0, MathHelper.cos(f) * 0.2F));
        }
    }

    protected void swimUpward() {
        this.setVelocity(this.getVelocity().add(0.0, 0.03999999910593033, 0.0));
    }

    private double getSwimHeight() {
        return (double)this.player.getStandingEyeHeight() < 0.4 ? 0.0 : 0.4;
    }

    private double getFluidHeight(TagKey<Fluid> tags) {
        return this.fluidHeight.getDouble(tags);
    }

    private boolean isInLava() {
        return this.fluidHeight.getDouble(FluidTags.LAVA) > 0.0;
    }

    private void checkWaterState() {
        var vehicle = player.getVehicle();
        if (vehicle instanceof BoatEntity) {
            if (!vehicle.isSubmergedInWater()) {
                this.touchingWater = false;
                return;
            }
        }

        if (updateMovementInFluid(FluidTags.WATER, 0.014)) {
            onLanding();
            this.touchingWater = true;
        } else {
            this.touchingWater = false;
        }
    }

    private void updateSwimming() {
        if (this.isSwimming) {
            isSwimming = isSprinting() && isTouchingWater() && !this.player.hasVehicle();
        } else {
            BlockPos blockPos = new BlockPos((int) this.getPos().x, (int) this.getPos().y, (int) this.getPos().z);
            FluidState fluidState = this.player.getWorld().getFluidState(blockPos);
            isSwimming = isSprinting() && this.isSubmergedInWater() &&
                    !this.player.hasVehicle() &&
                    fluidState.isIn(FluidTags.WATER);
        }
    }

    private void updateSubmergedInWaterState() {
        submergedInWater = this.submergedFluidTag.contains(FluidTags.WATER);
        submergedFluidTag.clear();

        double d = this.getEyeY() - 0.1111111119389534;
        Entity entity = this.player.getVehicle();

        if (entity instanceof BoatEntity boatEntity) {
            if (!boatEntity.isSubmergedInWater() && boatEntity.getBoundingBox().maxY >= d && boatEntity.getBoundingBox().minY <= d) {
                return;
            }
        }

        BlockPos blockPos = new BlockPos((int) this.getPos().x, (int) d, (int) this.getPos().z);
        FluidState fluidState = this.player.getWorld().getFluidState(blockPos);
        double e = blockPos.getY() + fluidState.getHeight(this.player.getWorld(), blockPos);

        if (e > d) {
            fluidState.streamTags().forEach(submergedFluidTag::add);
        }
    }



    private boolean updateMovementInFluid(TagKey<Fluid> tag, double speed) {
        if (this.isRegionUnloaded()) {
            return false;
        }

        Box box = this.getBoundingBox().contract(0.001);
        int i = MathHelper.floor(box.minX);
        int j = MathHelper.ceil(box.maxX);
        int k = MathHelper.floor(box.minY);
        int l = MathHelper.ceil(box.maxY);
        int m = MathHelper.floor(box.minZ);
        int n = MathHelper.ceil(box.maxZ);
        double d = 0.0;
        boolean bl = true;
        boolean bl2 = false;
        Vec3d vec3d = Vec3d.ZERO;
        int o = 0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int p = i; p < j; ++p) {
            for (int q = k; q < l; ++q) {
                for (int r = m; r < n; ++r) {
                    mutable.set(p, q, r);
                    FluidState fluidState = this.player.getWorld().getFluidState(mutable);
                    if (fluidState.isIn(tag)) {
                        double e = (q + fluidState.getHeight(this.player.getWorld(), mutable));
                        if (e >= box.minY) {
                            bl2 = true;
                            d = max(e - box.minY, d);
                            if (bl) {
                                Vec3d vec3d2 = fluidState.getVelocity(this.player.getWorld(), mutable);
                                if (d < 0.4) {
                                    vec3d2 = vec3d2.multiply(d);
                                }
                                vec3d = vec3d.add(vec3d2);
                                ++o;
                            }
                        }
                    }
                }
            }
        }

        if (vec3d.length() > 0.0) {
            if (o > 0) {
                vec3d = vec3d.multiply(1.0 / o);
            }

            Vec3d vec3d3 = this.getVelocity();
            vec3d = vec3d.multiply(speed * 1.0);
            double f = 0.003;

            if (abs(vec3d3.x) < 0.003 && abs(vec3d3.z) < 0.003 && vec3d.length() < 0.0045000000000000005) {
                vec3d = vec3d.normalize().multiply(0.0045000000000000005);
            }

            this.setVelocity(vec3d3.add(vec3d));
        }

        this.fluidHeight.put(tag, d);
        return bl2;
    }

    private boolean isRegionUnloaded() {
        Box box = this.getBoundingBox().expand(1.0);
        int i = MathHelper.floor(box.minX);
        int j = MathHelper.ceil(box.maxX);
        int k = MathHelper.floor(box.minZ);
        int l = MathHelper.ceil(box.maxZ);

        return !this.player.getWorld().isRegionLoaded(i, k, j, l);
    }

    private double getEyeY() {
        return this.pos.y + this.player.getStandingEyeHeight();
    }

    private void onLanding() {
        this.fallDistance = 0.0f;
    }
}