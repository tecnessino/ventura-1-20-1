package lol.ventura.features.modules.combat;

import lol.ventura.features.events.MotionEvent;
import lol.ventura.features.properties.NumberProperty;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.Comparator;
import java.util.List;

@ModuleDescriptor(name = "Aim Assist", category = Category.COMBAT, brief = "dyma mammmotha na 500PLN alior bank cashout")
public class AimAssist extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private final NumberProperty reach = new NumberProperty("Reach", 6.0f, 1.0f, 10.0f, 0.1f);

    public AimAssist(ModuleDescriptor descriptor) {
        super(descriptor);
        addSettings(reach);
    }

    public float[] rotation(Entity ent) {
        if (ent == null || mc.player == null) {
            return new float[]{mc.player.getYaw(), mc.player.getPitch()};
        }

        double deltaX = ent.getX() - mc.player.getX();
        double deltaZ = ent.getZ() - mc.player.getZ();
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        double deltaY = (ent.getY() + (ent.getHeight() * 0.5)) - (mc.player.getY() + mc.player.getStandingEyeHeight());

        float yaw = (float) (Math.atan2(deltaZ, deltaX) * (180.0 / Math.PI)) - 90.0F;

        float pitch = (float) (-Math.atan2(deltaY, distance) * (180.0 / Math.PI));

        return new float[]{yaw, pitch};
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private Entity findTarget() {
        List<LivingEntity> targets = mc.world.getEntitiesByClass(LivingEntity.class, mc.player.getBoundingBox().expand(reach.getValue()), entity -> entity != mc.player);
        return targets.stream().min(Comparator.comparingDouble(entity -> entity.squaredDistanceTo(mc.player))).orElse(null);
    }

    private final IEventListener<MotionEvent> motionEvent = event -> {
        if (mc.player == null || mc.world == null) return;
        Entity entity = findTarget();
        if (entity == null) return;

        if (mc.player.getPos().distanceTo(entity.getPos()) > reach.getValue()) return;

        float[] rotacja = rotation(entity);

        mc.player.setYaw(lerp(mc.player.getYaw(), rotacja[0], 0.1f));
        mc.player.setPitch(lerp(mc.player.getPitch(), rotacja[1], 0.1f));
    };
}
