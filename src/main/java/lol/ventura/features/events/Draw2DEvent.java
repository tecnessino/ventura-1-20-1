package lol.ventura.features.events;

import lol.ventura.foundation.event.IEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;

@AllArgsConstructor
@Getter @Setter
public class Draw2DEvent implements IEvent {
    DrawContext context;
}
