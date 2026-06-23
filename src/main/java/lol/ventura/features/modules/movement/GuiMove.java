package lol.ventura.features.modules.movement;

import lol.ventura.features.events.TickEvent;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@ModuleDescriptor(name = "InGuiMove", category = Category.MOVEMENT, brief = "victim chodzi z telefonem pod komisariat")
public class GuiMove extends Module {
    public GuiMove(ModuleDescriptor d){
        super(d);
    }

    private final IEventListener<TickEvent> onTick = e -> {
        if (mc.currentScreen instanceof HandledScreen<?>) {
            KeyBinding[] keys = new KeyBinding[]{
                    mc.options.forwardKey,
                    mc.options.backKey,
                    mc.options.leftKey,
                    mc.options.rightKey,
                    mc.options.jumpKey,
                    mc.options.sneakKey
            };

            for (KeyBinding key : keys) {
                boolean pressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), key.getDefaultKey().getCode());
                KeyBinding.setKeyPressed(key.getDefaultKey(), pressed);
            }
        }
    };
}
