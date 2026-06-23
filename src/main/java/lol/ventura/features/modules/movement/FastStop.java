package lol.ventura.features.modules.movement;

import lol.ventura.features.events.TickEvent;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lol.ventura.misc.player.MovementUtil;

@ModuleDescriptor(name = "Fast Stop", category = Category.MOVEMENT, brief = "Szybkie zatrzymanie")
public class FastStop extends Module {
    public FastStop(ModuleDescriptor descriptor) {
        super(descriptor);
    }

    private final IEventListener<TickEvent> updateEvent = event -> {
        if (!MovementUtil.moving() && mc.player.hurtTime == 0) {
            mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
        }
    };
}
