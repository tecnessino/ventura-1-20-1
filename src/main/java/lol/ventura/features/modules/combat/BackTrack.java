package lol.ventura.features.modules.combat;

import lol.ventura.features.events.GameTickEvent;
import lol.ventura.features.events.GameWorldChangeEvent;
import lol.ventura.features.events.NetworkPacketEvent;
import lol.ventura.features.events.PlayerAttackEvent;
import lol.ventura.features.properties.NumberProperty;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lol.ventura.foundation.module.ModuleRepository;
import lol.ventura.misc.math.DelayData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TrackedPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedHashSet;
import java.util.Set;

@ModuleDescriptor(name = "Back Track", category = Category.COMBAT, brief = "Wsadza ciezkiego chuja w przeciwnika")
public class BackTrack extends Module {
    private final NumberProperty minRange = new NumberProperty("Min Range", 3.0f, 0.0f, 6.0f, 0.01f);
    private final NumberProperty maxRange = new NumberProperty("Max Range", 5.0f, 0.0f, 6.0f, 0.01f);
    private final NumberProperty delay = new NumberProperty("Delay", 200, 0, 2000, 1);

    private static final Set<DelayData> packetQueue = new LinkedHashSet<>();

    private Entity target = null;
    private TrackedPosition position = null;

    public BackTrack(ModuleDescriptor descriptor) {
        super(descriptor);
        addSettings(minRange, maxRange, delay);
    }

    @Override
    protected void onEnable() {
        this.reset(false, false);
    }

    @Override
    protected void onDisable() {
        this.reset(true, false);
    }

    private final IEventListener<GameTickEvent> onGameTick = e -> {
        if (shouldCancel()) {
            processPackets(false);
        } else {
            reset(true, false);
        }
    };

    private final IEventListener<GameWorldChangeEvent> onWorldChange = e -> {
        if (e.getWorld() == null) {
            this.reset(true, true);
        }
    };

    private final IEventListener<PlayerAttackEvent> onAttack = e -> {
        var enemy = e.getEnemy();

        if (!isEnemy(enemy)) {
            return;
        }

        if (enemy != target) {
            this.reset(true, false);

            position = new TrackedPosition();
            position.setPos(enemy.getTrackedPosition().pos);
        }

        target = enemy;
    };

    private final IEventListener<NetworkPacketEvent> motionEvent = e -> {
        synchronized (packetQueue) {
            if (e.getType() != NetworkPacketEvent.Type.RECEIVED || e.isCancelled() || (packetQueue.isEmpty() && !shouldCancel())) {
                return;
            }

            var packet = e.getPacket();

            if (packet instanceof ChatMessageC2SPacket || packet instanceof GameMessageS2CPacket || packet instanceof CommandExecutionC2SPacket) {
                return;
            }

            if (packet instanceof PlayerPositionLookS2CPacket || packet instanceof DisconnectS2CPacket) {
                this.reset(true, false);
                return;
            }

            if (packet instanceof PlaySoundS2CPacket playSound) {
                if (playSound.getSound().value() == SoundEvents.ENTITY_PLAYER_HURT) {
                    return;
                }
            }

            if (packet instanceof HealthUpdateS2CPacket healthUpdate) {
                if (healthUpdate.getHealth() <= 0) {
                    this.reset(true, false);
                    return;
                }
            }

            if ((packet instanceof EntityS2CPacket entity && entity.getEntity(mc.world) == target) ||
                    (packet instanceof EntityPositionS2CPacket entityPosition && entityPosition.getId() == target.getId())) {
                Vec3d pos = null;
                if (packet instanceof EntityS2CPacket entity) {
                    pos = position != null ? position.withDelta(entity.getDeltaX(), entity.getDeltaY(), entity.getDeltaZ()) : null;

                } else if (packet instanceof EntityPositionS2CPacket entityPosition) {
                    pos = new Vec3d(entityPosition.getX(), entityPosition.getY(), entityPosition.getZ());

                }

                if (position != null) {
                    position.setPos(pos);
                }

                if (squaredBoxedDistanceTo(mc.player.getEyePos(), target.getBoundingBox().offset(pos.subtract(target.getPos()))) < squaredBoxedDistanceTo(mc.player.getEyePos(), target.getBoundingBox())) {
                    processPackets(true);

                    return;
                }
            }

            e.setCancelled(true);

            packetQueue.add(new DelayData(packet, System.currentTimeMillis()));
        }
    };

    private void handlePacket(Packet<?> packet) {
        try {
            ((Packet<ClientPlayPacketListener>) packet).apply(mc.getNetworkHandler());
        } catch (Exception ignored) {
        }
    }

    private void processPackets(boolean clear) {
        synchronized (packetQueue) {
            packetQueue.removeIf(data -> {
                if (clear || data.getDelay() <= System.currentTimeMillis() - this.delay.getValue().longValue()) {
                    handlePacket(data.getPacket());

                    return true;
                }

                return false;
            });
        }
    }

    private void reset(boolean handle, boolean clearOnly) {
        if (handle && !clearOnly) {
            processPackets(true);
        } else if (clearOnly) {
            synchronized (packetQueue) {
                packetQueue.clear();
            }
        }

        target = null;
        position = null;
    }

    private boolean isEnemy(Entity target) {
        return target instanceof PlayerEntity && target != mc.player &&
                boxedDistanceTo(target) >= minRange.getValue() &&
                boxedDistanceTo(target) <= maxRange.getValue() &&
                target.age > 10;
    }


    private boolean shouldCancel() {
        return target != null && target.isAlive() && isEnemy(target);
    }

    public static boolean isLagging() {
        return ModuleRepository.getInstance().getModule(BackTrack.class).isEnabled() && packetQueue.isEmpty();
    }

    public double boxedDistanceTo(Entity target) {
        return Math.sqrt(squaredBoxedDistanceTo(mc.player.getEyePos(), target.getBoundingBox()));
    }

    public double squaredBoxedDistanceTo(Vec3d position, Box box) {
        var pos = getNearest(position, box);

        return pos.squaredDistanceTo(position);
    }

    public Vec3d getNearest(Vec3d eyes, Box box) {
        double[] origin = {eyes.x, eyes.y, eyes.z};
        double[] destMin = {box.minX, box.minY, box.minZ};
        double[] destMax = {box.maxX, box.maxY, box.maxZ};

        for (int i = 0; i <= 2; i++) {
            origin[i] = Math.max(destMin[i], Math.min(origin[i], destMax[i]));
        }

        return new Vec3d(origin[0], origin[1], origin[2]);
    }
}
