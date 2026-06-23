package lol.ventura.features.events;

import lol.ventura.foundation.event.IEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.util.math.MatrixStack;

@Getter @Setter @AllArgsConstructor
public class Draw3DEvent implements IEvent {
    private MatrixStack stack;
}
