package lol.ventura.features.events;

import lol.ventura.foundation.event.IEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

@AllArgsConstructor
@Getter @Setter
public class CollisionEvent implements IEvent {
    private BlockState bs;
    private BlockPos bp;
}
