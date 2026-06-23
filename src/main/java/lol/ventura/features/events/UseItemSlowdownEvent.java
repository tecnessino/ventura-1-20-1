package lol.ventura.features.events;

import lol.ventura.foundation.event.IEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UseItemSlowdownEvent implements IEvent {
    private float forward, sideways;
}