package lol.ventura.misc.player;

import lol.ventura.foundation.GameAccessor;
import lombok.experimental.UtilityClass;
import net.minecraft.block.AirBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;


@UtilityClass
public class InventoryUtil implements GameAccessor {
    public static int getChestplate() {
        for (int i = 0; i < 45; ++i) {
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (itemStack.getItem() instanceof ArmorItem)
                if (((ArmorItem)itemStack.getItem()).getSlotType() == EquipmentSlot.CHEST)
                    return i == 40 ? 45 : i < 9 ? 36 + i : i;
        }
        return -1;
    }
    @Deprecated
    public static int getElytra() {
        for (ItemStack stack : mc.player.getInventory().armor) {
            if (stack.getItem() == Items.ELYTRA && stack.getDamage() < 430) {
                return -2;
            }
        }

        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.getItem() == Items.ELYTRA && s.getDamage() < 430) {
                slot = i;
                break;
            }
        }

        if (slot < 9 && slot != -1) {
            slot = slot + 36;
        }

        return slot;
    }

    public static SearchInvResult findInHotBar(Searcher searcher) {
        if (mc.player != null) {
            for (int i = 0; i < 9; ++i) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (searcher.isValid(stack)) {
                    return new SearchInvResult(i, true, stack);
                }
            }
        }

        return SearchInvResult.notFound();
    }

    public static SearchInvResult findItemInHotBar(List<Item> items) {
        return findInHotBar(stack -> items.contains(stack.getItem()));
    }

    public static SearchInvResult findItemInHotBar(Item... items) {
        return findItemInHotBar(Arrays.asList(items));
    }

    public static SearchInvResult findInInventory(Searcher searcher) {
        if (mc.player != null) {
            for (int i = 36; i >= 0; i--) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (searcher.isValid(stack)) {
                    if (i < 9) i += 36;
                    return new SearchInvResult(i, true, stack);
                }
            }
        }

        return SearchInvResult.notFound();
    }

    public static SearchInvResult findItemInInventory(List<Item> items) {
        return findInInventory(stack -> items.contains(stack.getItem()));
    }

    public static SearchInvResult findItemInInventory(Item... items) {
        return findItemInInventory(Arrays.asList(items));
    }

    public static int findItemSlot(Item item) {
        ClientPlayerEntity localPlayer = mc.player;

        int slot = -1;

        if (localPlayer == null) {
            return slot;
        }

        for (int i = 0; i < 36; i++) {
            ItemStack stack = localPlayer.getInventory().getStack(i);

            if (stack.getItem() == item) {
                slot = i;
                break;
            }
        }

        if (slot < 9 && slot != -1) {
            slot += 36;
        }

        return slot;
    }

    public static void moveItem(int one, int two, boolean swap) {
        mc.interactionManager.clickSlot(0, one, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(0, two, 0, SlotActionType.PICKUP, mc.player);
        if (swap) {
            mc.interactionManager.clickSlot(0, one, 0, SlotActionType.PICKUP, mc.player);
        }
    }

    public static void drop(int slot, boolean dropAll) {
        mc.interactionManager.clickSlot(0, slot, dropAll ? 1 : 0, SlotActionType.THROW, mc.player);
    }

    public static int findEmptySlot() {
        ClientPlayerEntity localPlayer = mc.player;

        if (localPlayer == null) {
            return -1;
        }

        int slot = -1;

        for (int i = 0; i < 36; i++) {
            ItemStack stack = localPlayer.getInventory().getStack(i);

            if (stack.isEmpty()) {
                slot = i;
                break;
            }
        }

        if (slot < 9 && slot != -1) {
            slot += 36;
        }

        return slot;
    }

    public static boolean isBetterArmor(ItemStack newStack, ItemStack currentStack) {
        if (!(newStack.getItem() instanceof ArmorItem newArmor) || !(currentStack.getItem() instanceof ArmorItem currentArmor)) {
            return false;
        }

        if (newArmor.getSlotType() != currentArmor.getSlotType()) return false;

        int newProtection = newArmor.getProtection() + EnchantmentHelper.getLevel(Enchantments.PROTECTION, newStack);
        int currentProtection = currentArmor.getProtection() + EnchantmentHelper.getLevel(Enchantments.PROTECTION, currentStack);

        float newToughness = newArmor.getToughness();
        float currentToughness = currentArmor.getToughness();

        if (newProtection != currentProtection) return newProtection > currentProtection;
        return newToughness > currentToughness;
    }

    public static boolean isBestArmor(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem targetArmor)) {
            return false;
        }

        EquipmentSlot targetSlot = targetArmor.getSlotType();
        ItemStack best = stack;

        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack current = mc.player.getInventory().getStack(i);

            if (!(current.getItem() instanceof ArmorItem currentArmor)) continue;
            if (currentArmor.getSlotType() != targetSlot) continue;

            if (isBetterArmor(current, best)) {
                return false;
            }
        }

        return true;
    }

    public interface Searcher {
        boolean isValid(ItemStack stack);
    }
}
