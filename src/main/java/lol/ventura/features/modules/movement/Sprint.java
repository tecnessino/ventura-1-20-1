package lol.ventura.features.modules.movement;

import lol.ventura.features.events.TickEvent;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;

@ModuleDescriptor(name = "Sprint", category = Category.MOVEMENT, brief = "Szybko mozesz wchodzic!!!")
public class Sprint extends Module {
    public Sprint(ModuleDescriptor descriptor) {
        super(descriptor);
    }

    public static boolean canSprint() {
        return mc.player.input.movementForward >= 0.1F && !mc.player.isSneaking();
    }

    private final IEventListener<TickEvent> updateEvent = event -> {
        if(mc.player == null || !canSprint())
            return;

        mc.player.setSprinting(true);
        mc.options.sprintKey.setPressed(true);
    };
}
