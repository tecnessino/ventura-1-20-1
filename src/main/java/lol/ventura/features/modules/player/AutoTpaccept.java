package lol.ventura.features.modules.player;

import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lol.ventura.features.events.NetworkPacketEvent;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;

@ModuleDescriptor(name = "AutoTpaccept", category = Category.PLAYER, brief = "Automatically accepts teleport requests")
public class AutoTpaccept extends Module {

    public AutoTpaccept(ModuleDescriptor descriptor) {
        super(descriptor);
    }

    private final IEventListener<NetworkPacketEvent> onPacket = event -> {
        if (event.getType() != NetworkPacketEvent.Type.RECEIVED) return;
        if (mc.player == null || mc.world == null) return;

        if (event.getPacket() instanceof GameMessageS2CPacket packet) {
            Text message = packet.content();
            String raw = message.getString();

            if (raw.contains("wysłał prośbe o teleportacje")) {
                mc.player.networkHandler.sendChatCommand("tpaccept " + "*");
            }
        }
    };
}
