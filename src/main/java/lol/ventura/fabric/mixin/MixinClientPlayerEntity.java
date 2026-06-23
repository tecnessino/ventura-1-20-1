package lol.ventura.fabric.mixin;

import com.mojang.authlib.GameProfile;
import lol.ventura.features.events.MovementPacketEvent;
import lol.ventura.features.events.TickEvent;
import lol.ventura.features.events.UseItemSlowdownEvent;
import lol.ventura.features.modules.movement.NoSlow;
import lol.ventura.features.modules.player.SafeWalk;
import lol.ventura.foundation.event.EventBus;
import lol.ventura.foundation.module.ModuleRepository;
import lol.ventura.foundation.rotation.Rotation;
import lol.ventura.foundation.rotation.RotationService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ALL")
@Mixin(value = ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity   extends AbstractClientPlayerEntity {
    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    protected boolean clipAtLedge()
    {
        return super.clipAtLedge()
                || ModuleRepository.getInstance().getModule(SafeWalk.class).isEnabled();
    }

    @Override
    protected Vec3d adjustMovementForSneaking(Vec3d movement, MovementType type)
    {
        Vec3d result = super.adjustMovementForSneaking(movement, type);

        if(movement != null)
            ModuleRepository.getInstance().getModule(SafeWalk.class).onClipAtLedge(!movement.equals(result));

        return result;
    }

    @Shadow
    protected abstract void sendSprintingPacket();

    @Shadow
    public abstract boolean isSneaking();

    @Shadow
    public boolean lastSneaking;

    @Shadow
    @Final
    public ClientPlayNetworkHandler networkHandler;

    @Shadow
    @Final
    protected MinecraftClient client;

    @Shadow
    protected abstract boolean isCamera();

    @Shadow
    private boolean autoJumpEnabled;

    @Shadow
    public Input input;

    @Shadow
    private double lastX;

    @Shadow
    private double lastBaseY;

    @Shadow
    private double lastZ;

    @Shadow
    private float lastYaw;

    @Shadow
    private float lastPitch;

    @Shadow
    private boolean lastOnGround;

    @Shadow
    private int ticksSinceLastPositionPacketSent;

    @Redirect(method = {"sendMovementPackets", "tick"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F"))
    private float hookSilentRotationYaw(ClientPlayerEntity instance) {
        Rotation rotation = RotationService.getInstance().getCurrentRotation();
        if (rotation == null) {
            return instance.getYaw();
        }

        return rotation.getYaw();
    }

    @Redirect(method = {"sendMovementPackets", "tick"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getPitch()F"))
    private float hookSilentRotationPitch(ClientPlayerEntity instance) {
        Rotation rotation = RotationService.getInstance().getCurrentRotation();
        if (rotation == null) {
            return instance.getPitch();
        }
        return rotation.getPitch();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V", shift = At.Shift.BEFORE, ordinal = 0))
    public void tick(CallbackInfo ci) {
        EventBus.getInstance().emit(new TickEvent());
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z", ordinal = 0))
    private void hookNoSlow(CallbackInfo callbackInfo) {
        final Input input = this.input;

        input.movementForward /= 0.2f;
        input.movementSideways /= 0.2f;

        final UseItemSlowdownEvent playerUseMultiplier = new UseItemSlowdownEvent(0.2f, 0.2f);
        EventBus.getInstance().emit(playerUseMultiplier);

        input.movementForward *= playerUseMultiplier.getForward();
        input.movementSideways *= playerUseMultiplier.getSideways();
    }

    @Redirect(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean canStartSprinting(ClientPlayerEntity playerEntity) {
        if (ModuleRepository.getInstance().getModule(NoSlow.class).isEnabled()) {
            return false;
        }

        return playerEntity.isUsingItem();
    }

    private float getFixedYaw() {
        final Rotation rotation = RotationService.getInstance().getCurrentRotation();

        if (rotation == null) {
            return this.getYaw();
        }

        return rotation.getYaw();
    }

    private float getFixedPitch() {
        final Rotation rotation = RotationService.getInstance().getCurrentRotation();

        if (rotation == null) {
            return this.getPitch();
        }

        return rotation.getPitch();
    }


    /**
     * @author groszus
     * @reason musialem dodac ten comment bo warning
     */
    @Overwrite
    private void sendMovementPackets() {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        MovementPacketEvent event = new MovementPacketEvent(this.getX(), this.getY(), this.getZ(), this.isOnGround(), true);
        EventBus.getInstance().emit(event);

        this.sendSprintingPacket();
        boolean bl = this.isSneaking();
        if (bl != this.lastSneaking) {
            ClientCommandC2SPacket.Mode mode = bl ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, mode));
            this.lastSneaking = bl;
        }

        if (this.isCamera()) {
            double posX = event.getX();
            double posY = event.getY();
            double posZ = event.getZ();
            boolean onGround = event.isOnGround();

            double d = posX - this.lastX;
            double e = posY - this.lastBaseY;
            double f = posZ - this.lastZ;

            float currentYaw = getFixedYaw();
            float currentPitch = getFixedPitch();

            double g = (double)(currentYaw - this.lastYaw);
            double h = (double)(currentPitch - this.lastPitch);

            ++this.ticksSinceLastPositionPacketSent;
            boolean bl2 = MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0E-4) || this.ticksSinceLastPositionPacketSent >= 20;
            boolean bl3 = g != (double)0.0F || h != (double)0.0F;
            if (this.hasVehicle()) {
                Vec3d vec3d = this.getVelocity();
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(vec3d.x, (double)-999.0F, vec3d.z, currentYaw, currentPitch, onGround));
                bl2 = false;
            } else if (bl2 && bl3) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(posX, posY, posZ, currentYaw, currentPitch, onGround));
            } else if (bl2) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(posX, posY, posZ, onGround));
            } else if (bl3) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(currentYaw, currentPitch, onGround));
            } else if (this.lastOnGround != onGround) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(onGround));
            }

            if (bl2) {
                this.lastX = posX;
                this.lastBaseY = posY;
                this.lastZ = posZ;
                this.ticksSinceLastPositionPacketSent = 0;
            }

            if (bl3) {
                this.lastYaw = currentYaw;
                this.lastPitch = currentPitch;
            }

            this.lastOnGround = onGround;
            this.autoJumpEnabled = (Boolean)this.client.options.getAutoJump().getValue();
        }

        EventBus.getInstance().emit(new MovementPacketEvent(this.getX(), this.getY(), this.getZ(), this.isOnGround(), false));
    }
}
