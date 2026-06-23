package lol.ventura.features.modules.render;

import lol.ventura.features.events.TickEvent;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

@ModuleDescriptor(
        name = "FullBright",
        category = Category.RENDER,
        brief = "Maximizes brightness to improve visibility.",
        key = 0
)
public class FullBright extends Module {
    private final int nightVisionDuration = StatusEffectInstance.INFINITE;

    public FullBright(ModuleDescriptor descriptor) {
        super(descriptor);
    }

    private void applyNightVision() {
        if (mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            StatusEffectInstance effect = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
            if (effect != null && effect.getDuration() < nightVisionDuration) {
                mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, nightVisionDuration, 0));
            }
        } else {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, nightVisionDuration, 0));
        }
    }

    private final IEventListener<TickEvent> tick = event -> {
        if (mc.player != null && !mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION))
            applyNightVision();
    };

    private void removeNightVision() {
        if (mc.player != null) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @Override
    protected void onEnable() {
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        removeNightVision();
    }
}