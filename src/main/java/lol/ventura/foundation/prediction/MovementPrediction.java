package lol.ventura.foundation.prediction;

import net.minecraft.util.math.Vec3d;

public interface MovementPrediction {
    Vec3d getPos();

    void tick();
}