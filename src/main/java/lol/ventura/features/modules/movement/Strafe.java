package lol.ventura.features.modules.movement;

import lol.ventura.features.events.TickEvent;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lol.ventura.misc.player.MovementUtil;

@ModuleDescriptor(name = "Strafe", category = Category.MOVEMENT, brief = "Strafuj se xdok")
public class Strafe extends Module {
    public Strafe(ModuleDescriptor descriptor) {
        super(descriptor);
    }

    private final IEventListener<TickEvent> updateEvent = event -> {
        MovementUtil.strafe(MovementUtil.speed());
    };
}
