package lol.ventura.features.events;

import lol.ventura.foundation.event.IEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class MovementPacketEvent implements IEvent {
    private final double x, y, z;
    private final boolean onGround;
    private final boolean pre;
}
