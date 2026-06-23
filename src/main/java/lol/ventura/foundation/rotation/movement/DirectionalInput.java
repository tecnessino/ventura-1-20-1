package lol.ventura.foundation.rotation.movement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.input.Input;

@Getter
@AllArgsConstructor
public class DirectionalInput {
    public final boolean forwards;
    public final boolean backwards;
    public final boolean left;
    public final boolean right;

    public DirectionalInput(Input input) {
        this(input.pressingForward, input.pressingBack, input.pressingLeft, input.pressingRight);
    }

    public static final DirectionalInput NONE = new DirectionalInput(false, false, false, false);
    public static final DirectionalInput FORWARDS = new DirectionalInput(true, false, false, false);
    public static final DirectionalInput BACKWARDS = new DirectionalInput(false, true, false, false);
}