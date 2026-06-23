package lol.ventura.misc.math;

import lombok.Data;
import net.minecraft.network.packet.Packet;

@Data
public class DelayData {
    private final Packet<?> packet;
    private final long delay;
}