package lol.ventura.features.modules.player;

import lol.ventura.features.events.TickEvent;
import lol.ventura.features.properties.NumberProperty;
import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lol.ventura.misc.player.InventoryUtil;
import lol.ventura.misc.math.Stopwatch;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@ModuleDescriptor(name = "AutoTotem", category = Category.PLAYER, brief = "seks")
public class AutoTotem extends Module {

    private boolean simswapped = false;
    private final Stopwatch stopwatch = new Stopwatch();

    private final NumberProperty health = new NumberProperty("Health", 5, 1, 20, 1);
    private final NumberProperty delay = new NumberProperty("Delay", 500, 0, 1000, 1);

    public AutoTotem(ModuleDescriptor descriptor) {
        super(descriptor);
        addSettings(health, delay);
    }

    private final IEventListener<TickEvent> onTick = e -> {
        if (mc.player == null || mc.interactionManager == null) return;

        if (stopwatch.elapsed(delay.getValue().longValue())) {
            if (mc.player.getHealth() <= health.getValue() && mc.currentScreen == null) {
                int totemSlot = InventoryUtil.findItemSlot(Items.TOTEM_OF_UNDYING);
                if (totemSlot != -1 && mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
                    mc.setScreen(new InventoryScreen(mc.player));
                    return;
                }
            }

            if (mc.player.getHealth() <= health.getValue()) {
                int totemSlot = InventoryUtil.findItemSlot(Items.TOTEM_OF_UNDYING);
                if (totemSlot != -1 && mc.currentScreen != null && mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
                    swapItem(totemSlot);
                    simswapped = true;
                    mc.setScreen(null);
                    stopwatch.reset();
                }
            } else if (simswapped && mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
                swapBack();
                simswapped = false;
            }
        }
    };

    private void swapBack() {
        int nearestSlot = findNearestCurrentItem();
        int prevCurrentItem = mc.player.getInventory().selectedSlot;

        int previousItemSlot = InventoryUtil.findItemSlot(mc.player.getOffHandStack().getItem());
        if (previousItemSlot != -1) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, previousItemSlot, nearestSlot, SlotActionType.SWAP, mc.player);

            GameAccessor.sendPacket(new UpdateSelectedSlotC2SPacket(nearestSlot));
            GameAccessor.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
            GameAccessor.sendPacket(new UpdateSelectedSlotC2SPacket(prevCurrentItem));
        }
    }

    public static int findNearestCurrentItem() {
        int i = mc.player.getInventory().selectedSlot;
        if (i == 8) return 7;
        if (i == 0) return 1;
        return i - 1;
    }

    public void swapItem(int slot) {
        int nearestSlot = findNearestCurrentItem();
        int prevCurrentItem = mc.player.getInventory().selectedSlot;

        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, nearestSlot, SlotActionType.SWAP, mc.player);

        GameAccessor.sendPacket(new UpdateSelectedSlotC2SPacket(nearestSlot));
        GameAccessor.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
        GameAccessor.sendPacket(new UpdateSelectedSlotC2SPacket(prevCurrentItem));
    }
}
