package lol.ventura.features.events;

import lol.ventura.foundation.event.IEvent;
import lol.ventura.foundation.rotation.movement.DirectionalInput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GameMoveInputEvent implements IEvent {
    private DirectionalInput directionalInput;
    private boolean jumping, sneaking;
}