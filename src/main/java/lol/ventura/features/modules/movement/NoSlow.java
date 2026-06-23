package lol.ventura.features.modules.movement;

import lol.ventura.features.events.MovementPacketEvent;
import lol.ventura.features.events.TickEvent;
import lol.ventura.features.events.UseItemSlowdownEvent;
import lol.ventura.features.properties.EnumProperty;
import lol.ventura.features.properties.NumberProperty;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Direction;

@ModuleDescriptor(name = "No Slowdown", category = Category.MOVEMENT, brief = "dzien dobry za spozniene")
public class NoSlow extends Module {
    private final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.VANILLA);
    public final NumberProperty forward = new NumberProperty("Forward multiplier", 1f, 0.01f, 1f, 0.01f);
    public final NumberProperty sideways = new NumberProperty("Sideways multiplier", 1f, 0.01f, 1f, 0.01f);

    public static int ticks = 0;

    public NoSlow(ModuleDescriptor descriptor) {
        super(descriptor);
        addSettings(mode, forward, sideways);
    }

    private final IEventListener<TickEvent> tickEvent = event -> {
        int selectedSlot = mc.player.getInventory().selectedSlot;

        switch (mode.getValue()) {
            case GRIM_LIMIT -> {
                if (mc.player.getActiveItem().getUseAction() == UseAction.EAT) {
                    mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(mc.player.getActiveHand() == Hand.OFF_HAND ? Hand.MAIN_HAND : Hand.OFF_HAND, 0));
                }
            }

            case GRIM -> {
                if (mc.player != null) {
                    if (mc.player.isUsingItem()) {
                        ticks++;
                    } else {
                        ticks = 0;
                    }
                }
            }

            case SWITCH -> {
                if (mc.player.getActiveItem().getUseAction() == UseAction.EAT) {
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot % 8 + 1));
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot % 7 + 2));
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(selectedSlot));
                }
            }
        }
    };

    private final IEventListener<UseItemSlowdownEvent> onSlowdown = event -> {
        if (mc.player.getActiveItem().getUseAction() == UseAction.NONE) {
            return;
        }

        switch (mode.getValue()) {
            case GRIM_LIMIT -> {
                if (mc.player.getItemUseTime() <= 4 && mc.player.getVelocity().y > -0.25) {
                    return;
                }
                event.setForward(forward.getValue().floatValue());
                event.setSideways(sideways.getValue().floatValue());
            }

            case GRIM -> {
                if (ticks >= 2f) {
                    event.setForward(forward.getValue().floatValue());
                    event.setSideways(sideways.getValue().floatValue());
                    ticks = 0;
                }
            }

            case INTAVE_LATEST -> {
                event.setForward(forward.getValue().floatValue());
                event.setSideways(sideways.getValue().floatValue());
            }
        }
    };

    private final IEventListener<MovementPacketEvent> onMove = event -> {
        switch (mode.getValue()) {
            case INTAVE_LATEST -> {
                if (mc.player.getActiveItem().getUseAction() != UseAction.DRINK || mc.player.getActiveItem().getUseAction() != UseAction.EAT || !event.isPre()) {
                    return;
                }

                if (mc.player.isUsingItem()) {
                    mc.getNetworkHandler().sendPacket(
                            new PlayerActionC2SPacket(
                                    PlayerActionC2SPacket.Action.RELEASE_USE_ITEM,
                                    mc.player.getBlockPos(),
                                    Direction.UP
                            )
                    );
                }
            }
        }
    };

    public enum Mode {
        VANILLA,
        GRIM,
        GRIM_LIMIT,
        SWITCH,
        INTAVE_LATEST
    }
}
