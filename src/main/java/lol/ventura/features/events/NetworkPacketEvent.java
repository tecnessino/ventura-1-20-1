package lol.ventura.features.events;

import lol.ventura.foundation.event.IEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.packet.Packet;

@Getter @Setter
@AllArgsConstructor
public class NetworkPacketEvent implements IEvent {
    private final Packet<?> packet;
    private final Type type;
    private boolean cancelled;

    public enum Type {
        SENT, RECEIVED
    }
}