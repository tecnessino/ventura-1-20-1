package lol.ventura.features.modules.player;

import lol.ventura.features.events.Draw2DEvent;
import lol.ventura.features.events.TickEvent;
import lol.ventura.features.properties.NumberProperty;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lol.ventura.misc.math.Stopwatch;
import lol.ventura.misc.render.RenderUtil;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ModuleDescriptor(name = "Chest Stealer", category = Category.PLAYER, brief = "kradnie victimowi kase na bulki")
public class ChestStealer extends Module {

    private final NumberProperty openChestDelay = new NumberProperty("Open chest delay", 100.0f, 0.0f, 1000.0f, 0.01f);
    private final NumberProperty closeChestDelay = new NumberProperty("Close chest delay", 100.0f, 0.0f, 1000.0f, 0.01f);
    private final NumberProperty delay = new NumberProperty("Delay", 50.0f, 0.0f, 1000.0f, 0.01f);
    private final NumberProperty missClickChance = new NumberProperty("Missclick chance", 10.0f, 0.0f, 100.0f, 1.0f);

    private final Stopwatch timer = new Stopwatch(),
            openTimer = new Stopwatch(),
            closeTimer = new Stopwatch(),
            highlightTimer = new Stopwatch();

    private final Random random = new Random();

    private int currentSlot = -1;

    public ChestStealer(ModuleDescriptor descriptor) {
        super(descriptor);
        addSettings(delay, openChestDelay, closeChestDelay, missClickChance);
    }

    private final IEventListener<TickEvent> onTick = event -> {
        if (mc.player == null) return;

        if (mc.currentScreen instanceof GenericContainerScreen screen) {
            if (!openTimer.elapsedDouble(openChestDelay.getValue())) {
                return;
            }

            GenericContainerScreenHandler handler = screen.getScreenHandler();

            if (random.nextFloat() * 100 < missClickChance.getValue() && !timer.elapsedDouble(delay.getValue())) {
                return;
            }

            if (timer.elapsedDouble(delay.getValue())) {
                if (random.nextFloat() * 100 < missClickChance.getValue()) {
                    List<Integer> emptySlots = new ArrayList<>();
                    for (int i = 0; i < handler.getInventory().size(); i++) {
                        ItemStack stack = handler.getInventory().getStack(i);
                        if (stack.isEmpty()) {
                            emptySlots.add(i);
                        }
                    }

                    if (!emptySlots.isEmpty()) {
                        int randomEmptySlot = emptySlots.get(random.nextInt(emptySlots.size()));

                        currentSlot = randomEmptySlot;
                        highlightTimer.reset();

                        mc.interactionManager.clickSlot(handler.syncId, randomEmptySlot, 0, SlotActionType.PICKUP, mc.player);
                        timer.reset();
                        return;
                    }
                }

                for (int i = 0; i < handler.getInventory().size(); i++) {
                    ItemStack stack = handler.getInventory().getStack(i);

                    if (!stack.isEmpty()) {
                        currentSlot = i;
                        highlightTimer.reset();

                        mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                        timer.reset();
                        closeTimer.reset();
                        return;
                    }
                }

                currentSlot = -1;
            }

            if (!closeTimer.elapsedDouble(closeChestDelay.getValue())) {
                return;
            }

            mc.player.closeHandledScreen();
        } else {
            openTimer.reset();
            closeTimer.reset();
            currentSlot = -1;
        }
    };

    private final IEventListener<Draw2DEvent> onRender = event -> {
        if (mc.player == null || mc.currentScreen == null) return;
        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) return;

        if (currentSlot != -1 && !highlightTimer.elapsed(500)) {
            final GenericContainerScreenHandler handler = screen.getScreenHandler();

            if (currentSlot < handler.slots.size()) {
                final Slot slot = handler.slots.get(currentSlot);

                int x = ((screen.width - screen.backgroundWidth) / 2) + slot.x;
                int y = ((screen.height - screen.backgroundHeight) / 2) + slot.y;

                RenderUtil.drawRect(event.getContext().getMatrices(), x, y, 16, 16, new Color(255, 255, 255, 100));
            }
        }
    };
}