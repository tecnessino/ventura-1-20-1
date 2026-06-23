package lol.ventura.features.modules.render;

import lol.ventura.features.events.TickEvent;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;

import java.util.List;

@ModuleDescriptor(name = "No Bad Effects", category = Category.RENDER, brief = "usuwa rzygi z ekranu")
public class NoBadEffects extends Module {
    public NoBadEffects(ModuleDescriptor descriptor) {
        super(descriptor);
    }

    private final IEventListener<TickEvent> updateEvent = event -> {
        for (StatusEffect effect : List.of(
                StatusEffects.BLINDNESS,
                StatusEffects.NAUSEA
        )) {
            if (mc.player.hasStatusEffect(effect)) {
                mc.player.removeStatusEffect(effect);
            }
        }
    };
}
