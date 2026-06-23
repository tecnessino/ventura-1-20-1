package lol.ventura.features.events;

import lol.ventura.foundation.event.IEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.Vec3d;

@Getter
@Setter
@AllArgsConstructor
public class PlayerVelocityStrafeEvent implements IEvent {
    private Vec3d movementInput, velocity;
    private float speed, yaw;
}