package lol.ventura.features.modules.movement;

import lol.ventura.features.events.TickEvent;
import lol.ventura.features.properties.NumberProperty;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lol.ventura.misc.player.MovementUtil;
import org.lwjgl.glfw.GLFW;

@ModuleDescriptor(name = "Flight", category = Category.MOVEMENT, brief = "Zapierdalac szybko", key = GLFW.GLFW_KEY_F)
public class Flight extends Module {
    private final NumberProperty speed = new NumberProperty("Speed", 1f, 0f, 10f, 0.01f);

    public Flight(ModuleDescriptor descriptor) {
        super(descriptor);
        addSettings(speed);
    }

    private final IEventListener<TickEvent> updateEvent = event -> {
        final float y = mc.player.input.jumping ? speed.getValue().floatValue()
                : mc.player.input.sneaking ? -speed.getValue().floatValue()
                : 0;

        MovementUtil.strafe(speed.getValue());
        mc.player.setVelocity(mc.player.getVelocity().x, y, mc.player.getVelocity().z);
    };
}
