package lol.ventura.features.modules.combat;

import lol.ventura.features.events.MotionEvent;
import lol.ventura.features.properties.BooleanProperty;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.hit.HitResult;

@ModuleDescriptor(name = "Trigger Bot", category = Category.COMBAT, brief = "nie uzywa ciezkiego chuja do bicia victimow")
public class TriggerBot extends Module {

    private final BooleanProperty usuwacztarcz = new BooleanProperty("Shield Breaker", true);

    public TriggerBot(ModuleDescriptor descriptor) {
        super(descriptor);
        addSettings(usuwacztarcz);
    }

    private final IEventListener<MotionEvent> motionEvent = event -> {
        if (mc.player.getAttackCooldownProgress(0.5f) >= 1.0f) {
            //if (!crit()) {
            //    return;
            // }
            if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;
            if (mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                //GameAccessor.sendChatMessage(String.valueOf(mc.player.getAttackCooldownProgress(mc.getTickDelta())));
                KeyBinding.onKeyPressed(mc.options.attackKey.getDefaultKey());
            }
        }
    };


    private boolean crit() {
        if (mc.player.getAttackCooldownProgress(0.5f) < 0.9F) {
            return false;
        }
        boolean isOnGround = mc.player.isOnGround();
        if (!isOnGround && mc.player.fallDistance > 0.0f && mc.player.fallDistance < 1.14f) {
            return true;
        }
        return false;
    }
}
