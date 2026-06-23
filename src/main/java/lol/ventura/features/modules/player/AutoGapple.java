package lol.ventura.features.modules.player;

import lol.ventura.features.events.TickEvent;
import lol.ventura.features.properties.NumberProperty;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@ModuleDescriptor(name = "AutoGap", category = Category.PLAYER, brief = "wpierdala sperme")
public class AutoGapple extends Module {

    private final NumberProperty health = new NumberProperty("Health", 10, 0, 20, 1);

    public AutoGapple(ModuleDescriptor desc) {
        super(desc);
        addSettings(health);
    }
    boolean eating = false;

    private final IEventListener<TickEvent> onTick = e -> {
        if (mc.player.getHealth() < health.getValue()) {
            if (isHoldingGoldenApple() && !isGoldenAppleOnCooldown()) {
                if (!eating && mc.currentScreen == null && !mc.player.isUsingItem()) {
                    eating = true;
                    mc.options.useKey.setPressed(true);
                }
            } else {
                if (eating) {
                    eating = false;
                    mc.options.useKey.setPressed(false);
                }
            }
        }
        if (eating && mc.currentScreen == null && !mc.player.isUsingItem()) {
            mc.options.useKey.setPressed(true);
        }
    };


    private boolean isHoldingGoldenApple() {
        return isGoldenApple(mc.player.getMainHandStack()) || isGoldenApple(mc.player.getOffHandStack());
    }

    private boolean isGoldenApple(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE;
    }

    private boolean isGoldenAppleOnCooldown() {
        return mc.player.getItemCooldownManager().isCoolingDown(Items.GOLDEN_APPLE) ||
                mc.player.getItemCooldownManager().isCoolingDown(Items.ENCHANTED_GOLDEN_APPLE);
    }
}
