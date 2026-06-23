package lol.ventura.features.modules.render;

import lol.ventura.features.events.NetworkPacketEvent;
import lol.ventura.features.events.TickEvent;
import lol.ventura.features.properties.EnumProperty;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

@ModuleDescriptor(name = "Time Changer", category = Category.RENDER, brief = "changes world time")
public class TimeChanger extends Module {
    private final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.DAY);

    public TimeChanger(ModuleDescriptor descriptor) {
        super(descriptor);
        addSettings(mode);
    }

    private final IEventListener<NetworkPacketEvent> onPacketEvent = event -> {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            event.setCancelled(true);

            if (mc.world != null) {
                long time = switch (mode.getValue()) {
                    case DAY -> 1000;
                    case NIGHT -> 13000;
                    case SUNSET -> 12000;
                    case MIDNIGHT -> 18000;
                };

                mc.world.setTimeOfDay(time);
            }
        }
    };

    private final IEventListener<TickEvent> onTick = event -> {
        if (mc.world != null) {
            long time = switch (mode.getValue()) {
                case DAY -> 1000;
                case NIGHT -> 13000;
                case SUNSET -> 12000;
                case MIDNIGHT -> 18000;
            };
            mc.world.setTimeOfDay(time);
        }
    };

    public enum Mode {
        DAY,
        NIGHT,
        SUNSET,
        MIDNIGHT
    }
}
