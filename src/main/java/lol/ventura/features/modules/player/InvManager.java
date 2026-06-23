package lol.ventura.features.modules.player;

import lol.ventura.features.properties.NumberProperty;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lol.ventura.features.events.TickEvent;
import lol.ventura.misc.math.Stopwatch;
import lol.ventura.misc.player.InventoryUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.UseAction;

@ModuleDescriptor(name = "InventoryManager", category = Category.PLAYER, brief = "a")
public class InvManager extends Module {

    private final NumberProperty delay = new NumberProperty("Delay", 250, 0, 500, 1);

    private final Stopwatch stopwatch = new Stopwatch();

    public InvManager(ModuleDescriptor descriptor) {
        super(descriptor);
        addSettings(delay);
    }

    private final IEventListener<TickEvent> onTick = event -> {
        if (mc.world == null || mc.player == null) return;
        if (!(mc.currentScreen instanceof InventoryScreen)) return;
        if (!stopwatch.elapsed(delay.getValue().longValue())) return;

        ScreenHandler screenHandler = mc.player.currentScreenHandler;
        Slot bestSword = null, bestPickaxe = null, bestAxe = null, bestShovel = null;

        int swordSlot = 36;
        int pickaxeSlot = 43;
        int axeSlot = 37;
        int shovelSlot = 42;

        for (Slot slot : screenHandler.slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof SwordItem &&
                    (mc.player.getInventory().getStack(swordSlot).isEmpty() || isBetterSword(bestSword != null ? bestSword.getStack() : null, stack))) {
                bestSword = slot;
            }
            if (stack.getItem() instanceof PickaxeItem &&
                    (mc.player.getInventory().getStack(pickaxeSlot).isEmpty() || isBetterTool(bestPickaxe != null ? bestPickaxe.getStack() : null, stack))) {
                bestPickaxe = slot;
            }
            if (stack.getItem() instanceof AxeItem &&
                    (mc.player.getInventory().getStack(axeSlot).isEmpty() || isBetterTool(bestAxe != null ? bestAxe.getStack() : null, stack))) {
                bestAxe = slot;
            }
            if (stack.getItem() instanceof ShovelItem &&
                    (mc.player.getInventory().getStack(shovelSlot).isEmpty() || isBetterTool(bestShovel != null ? bestShovel.getStack() : null, stack))) {
                bestShovel = slot;
            }
        }

        for (Slot slot : screenHandler.slots) {
            if (!stopwatch.elapsed(delay.getValue().longValue())) break;
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            if (!isGood(stack)
                    && slot != bestSword && slot != bestPickaxe && slot != bestAxe && slot != bestShovel) {
                InventoryUtil.drop(slot.id, true);
                stopwatch.reset();
            }
        }

        if (bestSword != null && stopwatch.elapsed(delay.getValue().longValue())) {
            ItemStack current = mc.player.getInventory().getStack(swordSlot);
            if (bestSword.id != swordSlot &&
                    (!areStacksExactlyEqual(current, bestSword.getStack()) &&
                            (current.isEmpty() || isBetterSword(current, bestSword.getStack())))) {
                InventoryUtil.moveItem(bestSword.id, swordSlot, !current.isEmpty());
                stopwatch.reset();
            }
        }

        if (bestPickaxe != null && stopwatch.elapsed(delay.getValue().longValue())) {
            ItemStack current = mc.player.getInventory().getStack(pickaxeSlot);
            if (bestPickaxe.id != pickaxeSlot &&
                    (!areStacksExactlyEqual(current, bestPickaxe.getStack()) &&
                            (current.isEmpty() || isBetterTool(current, bestPickaxe.getStack())))) {
                InventoryUtil.moveItem(bestPickaxe.id, pickaxeSlot, !current.isEmpty());
                stopwatch.reset();
            }
        }

        if (bestAxe != null && stopwatch.elapsed(delay.getValue().longValue())) {
            ItemStack current = mc.player.getInventory().getStack(axeSlot);
            if (bestAxe.id != axeSlot &&
                    (!areStacksExactlyEqual(current, bestAxe.getStack()) &&
                            (current.isEmpty() || isBetterTool(current, bestAxe.getStack())))) {
                InventoryUtil.moveItem(bestAxe.id, axeSlot, !current.isEmpty());
                stopwatch.reset();
            }
        }

        if (bestShovel != null && stopwatch.elapsed(delay.getValue().longValue())) {
            ItemStack current = mc.player.getInventory().getStack(shovelSlot);
            if (bestShovel.id != shovelSlot &&
                    (!areStacksExactlyEqual(current, bestShovel.getStack()) &&
                            (current.isEmpty() || isBetterTool(current, bestShovel.getStack())))) {
                InventoryUtil.moveItem(bestShovel.id, shovelSlot, !current.isEmpty());
                stopwatch.reset();
            }
        }
    };

    private boolean areStacksExactlyEqual(ItemStack a, ItemStack b) {
        if (a == null || b == null) return false;
        if (a.isEmpty() && b.isEmpty()) return true;
        if (!ItemStack.canCombine(a, b)) return false;
        return ItemStack.areEqual(a, b);
    }

    public boolean isBetterSword(ItemStack currentSword, ItemStack consideredSword) {
        if (consideredSword == null || consideredSword.isEmpty()) return false;
        if (currentSword == null || currentSword.isEmpty()) return true;

        float currentDamage = getBaseDamage(currentSword);
        float consideredDamage = getBaseDamage(consideredSword);

        int currentSharpness = EnchantmentHelper.getLevel(Enchantments.SHARPNESS, currentSword);
        int consideredSharpness = EnchantmentHelper.getLevel(Enchantments.SHARPNESS, consideredSword);

        if (currentSharpness > 0)
            currentDamage += 1.0F + (currentSharpness - 1) * 0.5F;
        if (consideredSharpness > 0)
            consideredDamage += 1.0F + (consideredSharpness - 1) * 0.5F;

        return consideredDamage > currentDamage;
    }

    public boolean isBetterTool(ItemStack currentTool, ItemStack consideredTool) {
        if (consideredTool == null || currentTool == null || consideredTool.isEmpty() || currentTool.isEmpty()) return true;

        float currentBreakingStrength = getBreakingStrength(currentTool);
        float consideredBreakingStrength = getBreakingStrength(consideredTool);

        return consideredBreakingStrength > currentBreakingStrength;
    }

    public float getBreakingStrength(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0.0F;
        if (stack.getItem() instanceof ToolItem) {
            ToolItem tool = (ToolItem) stack.getItem();
            BlockState air = Blocks.AIR.getDefaultState();
            return tool.getMiningSpeedMultiplier(stack, air);
        }
        return 0.0F;
    }

    public float getBaseDamage(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0.0F;
        if (stack.getItem() instanceof ToolItem) {
            return ((ToolItem) stack.getItem()).getMaxDamage();
        }
        return 0.0F;
    }

    private boolean isGood (ItemStack stack){
        return stack.getItem() instanceof ShieldItem
                || stack.getUseAction().equals(UseAction.EAT)
                || stack.getItem() instanceof EnderPearlItem
                || stack.getItem() instanceof BlockItem
                || stack.getItem() instanceof FishingRodItem
                || stack.getItem() instanceof PotionItem
                || stack.getItem() instanceof ArmorItem && (
                InventoryUtil.isBestArmor(stack) || InventoryUtil.isBetterArmor(stack, mc.player.getInventory().getArmorStack(((ArmorItem)stack.getItem()).getSlotType().getEntitySlotId())))
                || stack.getItem() instanceof ElytraItem
                || stack.getItem() == Items.WATER_BUCKET
                || stack.getItem() == Items.TOTEM_OF_UNDYING;
    }

    @Override
    public void onEnable() {
        stopwatch.reset();
    }

    @Override
    public void onDisable() {
    }
}
