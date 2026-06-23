package lol.ventura.features.modules.player;

import net.minecraft.block.AirBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import lol.ventura.features.events.TickEvent;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;

import java.util.ArrayList;
import java.util.List;

@ModuleDescriptor(name = "AutoTool", category = Category.PLAYER, brief = "zmienia tool")
public class AutoTool extends Module {

    private boolean swap;
    private long swapDelay;
    private final List<Integer> lastItem = new ArrayList<>();

    public AutoTool(ModuleDescriptor d) {
        super(d);
    }

    private final IEventListener<TickEvent> onTick = event -> {
        if (!(mc.crosshairTarget instanceof BlockHitResult result))
            return;
        BlockPos pos = result.getBlockPos();
        if (mc.world.getBlockState(pos).isAir()) return;

        int tool = getTool(pos);
        if (tool != -1 && mc.options.attackKey.isPressed()) {
            lastItem.add(mc.player.getInventory().selectedSlot);
            mc.player.getInventory().selectedSlot = tool;
            swap = true;
            swapDelay = System.currentTimeMillis();
        } else if (swap && !lastItem.isEmpty() && System.currentTimeMillis() >= swapDelay + 300) {
                mc.player.getInventory().selectedSlot = lastItem.get(0);
                lastItem.clear();
                swap = false;
        }
    };


    private int getTool(BlockPos pos) {
        int index = -1;
        float currentFastest = 1.0f;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack == ItemStack.EMPTY) continue;

            float destroySpeed = stack.getMiningSpeedMultiplier(mc.world.getBlockState(pos));
            if (destroySpeed <= 1.0f) continue;

            float digSpeed = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
            float totalSpeed = destroySpeed + (destroySpeed > 1.0f ? digSpeed * digSpeed + 1 : 0);

            if (mc.world.getBlockState(pos).getBlock() instanceof AirBlock) return -1;

            if (mc.world.getBlockState(pos).getBlock() instanceof EnderChestBlock) {
                if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, stack) > 0 && totalSpeed > currentFastest) {
                    currentFastest = totalSpeed;
                    index = i;
                }
            } else if (totalSpeed > currentFastest) {
                currentFastest = totalSpeed;
                index = i;
            }
        }
        return index;
    }
}
