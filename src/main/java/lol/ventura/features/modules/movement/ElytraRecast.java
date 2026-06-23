package lol.ventura.features.modules.movement;

import lol.ventura.features.events.TickEvent;
import lol.ventura.features.ui.Notify;
import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

//grim 2025 bypass made by alan wood
@ModuleDescriptor(name = "Elytra Recast", category = Category.MOVEMENT, brief = "mega latanie")
public class ElytraRecast extends Module {
    public ElytraRecast(ModuleDescriptor d) {
        super(d);
    }

    @Override
    protected void onEnable() {
        super.onEnable();
    }

    private final IEventListener<TickEvent> onTick = e -> {
        if (mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
            //Notify.getInstance().queue(new Notify.Notification(Notify.NotificationSeverity.WARNING, getDescriptor().name(), "Disabled due to missing Elytra.", 5000));
            return;
        }
            if (mc.player.isOnGround() && !mc.options.jumpKey.isPressed()) {
                mc.player.jump();
            }
            if (!mc.player.isOnGround() && !mc.player.isFallFlying()) {
                mc.player.startFallFlying();
                GameAccessor.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
    };
}
