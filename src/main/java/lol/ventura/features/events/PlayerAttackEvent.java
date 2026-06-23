package lol.ventura.features.events;

import lol.ventura.foundation.event.IEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;

@Getter
@AllArgsConstructor
public class PlayerAttackEvent implements IEvent {
    private final Entity enemy;
}