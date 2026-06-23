package lol.ventura.features.modules.combat;

import lol.ventura.features.events.PlayerAttackEvent;
import lol.ventura.features.properties.EnumProperty;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

@ModuleDescriptor(name = "Criticals", category = Category.COMBAT, brief = "skacze na chuju kolegi")
public class Criticals extends Module {

    private EnumProperty<cipka> tryb = new EnumProperty<>("Mode",cipka.CWEL);

    public Criticals(ModuleDescriptor descriptor) {
        super(descriptor);
        addSettings(tryb);
    }

        private final IEventListener<PlayerAttackEvent> onAttack = event -> {
        if (mc.player == null || mc.world == null) return;

        if (!(event.getEnemy() instanceof EndCrystalEntity) && !mc.player.isSubmergedInWater() && !mc.player.isInLava()) {
            switch (tryb.getValue()) {
                case CWEL:
                    penis(-0.000001);
                    penis(-0.000000);
                   break;
                case JUMP:
                    mc.player.jump();
                    break;
            }
        }
    };

    private void penis(double y){
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + y, mc.player.getZ(), false));
    }

    public enum cipka{
        CWEL,
        JUMP
    }
}
