package lol.ventura.features.modules.combat;

import lol.ventura.features.events.PlayerAttackEvent;
import lol.ventura.features.properties.EnumProperty;
import lol.ventura.features.properties.NumberProperty;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lol.ventura.misc.math.MathUtil;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;

@ModuleDescriptor(name = "Velocity", category = Category.COMBAT, brief = "skacze sobie????")
public class Velocity extends Module {

    private final NumberProperty jumpChance = new NumberProperty("Jump Chance", 90, 0, 100, 1);
    private final NumberProperty accuracy = new NumberProperty("Accuracy", 100, 0, 100, 1);

    private final EnumProperty<mode> vmode = new EnumProperty<>("Mode", mode.JUMP);

    public Velocity(ModuleDescriptor descriptor) {
        super(descriptor);
        addSettings(vmode, jumpChance, accuracy);
    }

    private final IEventListener<PlayerAttackEvent> onTick = event -> {
        switch (vmode.getValue()) {
            case JUMP:
                if(!(event.getEnemy() instanceof PlayerEntity))
                    break;

                PlayerEntity victim = (PlayerEntity) event.getEnemy();
                if (!(mc.currentScreen instanceof HandledScreen)
                        && victim.isOnGround()
                        && victim.hurtTime > 0
                        && victim.hurtTime < 10
                        && victim.hurtTime == victim.maxHurtTime - 1
                        && MathUtil.random(0, 100) <= jumpChance.getValue()
                        && MathUtil.random(0, 100) <= accuracy.getValue()) {
                    mc.options.jumpKey.setPressed(true);
                }
                break;
        }
    };

    public enum mode {
        JUMP
    }
}
