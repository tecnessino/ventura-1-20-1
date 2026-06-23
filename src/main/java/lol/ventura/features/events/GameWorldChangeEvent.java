package lol.ventura.features.events;

import lol.ventura.foundation.event.IEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.world.ClientWorld;

@Getter
@AllArgsConstructor
public class GameWorldChangeEvent implements IEvent {
    private final ClientWorld world;
}