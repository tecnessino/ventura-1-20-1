package lol.ventura.features.modules.player;

import lol.ventura.features.properties.NumberProperty;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lol.ventura.features.events.TickEvent;
import lol.ventura.misc.math.Stopwatch;
import lol.ventura.misc.player.InventoryUtil;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

@ModuleDescriptor(name = "AutoArmor", category = Category.PLAYER, brief = "b")
public class AutoArmor extends Module {

    private final NumberProperty delay = new NumberProperty("Delay", 150, 0, 500, 1);
    private final Stopwatch stopwatch = new Stopwatch();

    public AutoArmor(ModuleDescriptor descriptor) {
        super(descriptor);
        addSettings(delay);
    }

    private final IEventListener<TickEvent> onTick = event -> {
        if (mc.world == null || mc.player == null) return;
        if (!(mc.currentScreen instanceof InventoryScreen)) return;
        if (!stopwatch.elapsed(delay.getValue().longValue())) return;

        ScreenHandler screenHandler = mc.player.currentScreenHandler;

        for (Slot slot : screenHandler.slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem)) continue;

            ArmorItem armorItem = (ArmorItem) stack.getItem();
            EquipmentSlot equipmentSlot = armorItem.getSlotType();

            int armorSlotIndex = switch (equipmentSlot) {
                case HEAD -> 5;
                case CHEST -> 6;
                case LEGS -> 7;
                case FEET -> 8;
                default -> -1;
            };

            if (armorSlotIndex == -1) continue;

            ItemStack currentArmor = mc.player.getInventory().getArmorStack(equipmentSlot.getEntitySlotId());

            if (currentArmor.isEmpty() || InventoryUtil.isBetterArmor(stack, currentArmor)) {
                InventoryUtil.moveItem(slot.id, armorSlotIndex, !currentArmor.isEmpty());
                stopwatch.reset();
                break;
            }
        }
    };

    @Override
    public void onEnable() {
        stopwatch.reset();
    }

    @Override
    public void onDisable() {
    }
}
