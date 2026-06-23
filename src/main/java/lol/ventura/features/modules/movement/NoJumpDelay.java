package lol.ventura.features.modules.movement;

import lol.ventura.features.events.TickEvent;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;

@ModuleDescriptor(name = "No Jump Delay", category = Category.MOVEMENT, brief = "Brak delay)) pravite")
public class NoJumpDelay extends Module {
    public NoJumpDelay(ModuleDescriptor descriptor) {
        super(descriptor);
    }

    private final IEventListener<TickEvent> updateEvent = event -> {
        mc.player.jumpingCooldown = 0;
    };
}
