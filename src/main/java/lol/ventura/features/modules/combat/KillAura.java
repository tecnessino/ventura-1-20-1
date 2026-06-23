package lol.ventura.features.modules.combat;

import lol.ventura.features.combat.CombatService;
import lol.ventura.features.events.TickEvent;
import lol.ventura.features.properties.BooleanProperty;
import lol.ventura.features.properties.EnumProperty;
import lol.ventura.features.properties.MultiProperty;
import lol.ventura.features.properties.NumberProperty;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lol.ventura.foundation.rotation.Rotation;
import lol.ventura.foundation.rotation.RotationService;
import lol.ventura.misc.math.FastNoise;
import lol.ventura.misc.math.RotationUtil;
import lol.ventura.misc.math.Stopwatch;
import lol.ventura.misc.request.Priority;
import lombok.Getter;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;

@Getter
@ModuleDescriptor(name = "Kill Aura", category = Category.COMBAT, brief = "Uzywa cieziego chuja do bicia victimow", key = GLFW.GLFW_KEY_R)
public class KillAura extends Module {

    public final EnumProperty<RotationMode> rotationMode = new EnumProperty<>("Rotations", RotationMode.LINEAR);
    private final NumberProperty range = new NumberProperty("Rotation range", 3.0f, 3.0f, 6.0f, 0.01f);
    public final NumberProperty minRotationSpeed = new NumberProperty("Rotation speed (min)", 90.0f, 0.01f, 180f, 0.01f);
    public final NumberProperty maxRotationSpeed = new NumberProperty("Rotation speed (max)", 90.0f, 0.01f, 180f, 0.01f);
    public final NumberProperty threshold = new NumberProperty("Reset threshold", 3f, 0f, 20f, 0.1f);
    public final NumberProperty ticksTillReset = new NumberProperty("Ticks till reset", 3, 0, 20, 1);

    private final NumberProperty attackRange = new NumberProperty("Attack range", 3.0f, 3.0f, 6.0f, 0.01f);
    private final MultiProperty<ClickMode> attackMode  = new MultiProperty<>("Attack mode", false, ClickMode.ONE_POINT_NINE);

    private final NumberProperty minCPS = new NumberProperty("Constant CPS (min)", 10, 0, 20, 1);
    private final NumberProperty maxCPS = new NumberProperty("Constant CPS (max)", 15, 0, 20, 1);

    private final MultiProperty<Targets> targets = new MultiProperty<>("Targets", true, Targets.PLAYERS);
    private final MultiProperty<Extensions> extensions = new MultiProperty<>("Extensions", true, Extensions.RAYCAST, Extensions.MOVEMENT_FIX, Extensions.FAKE_BLOCKING);

    public boolean doVisualBlock = false;
    public static LivingEntity target = null;

    private final FastNoise noise = new FastNoise();
    private final FastNoise cpsNoise = new FastNoise();
    private final Stopwatch stopwatch = new Stopwatch();

    private float cpsTime = 0.0f;
    private double currentClickDelay = 0.0;

    public KillAura(ModuleDescriptor descriptor) {
        super(descriptor);
        addSettings(rotationMode, range, attackRange, attackMode, minCPS, maxCPS, minRotationSpeed, maxRotationSpeed, ticksTillReset, threshold, targets, extensions);

        cpsNoise.SetSeed((int) System.currentTimeMillis());
        cpsNoise.SetNoiseType(FastNoise.NoiseType.Simplex);
        cpsNoise.SetFrequency(0.35f);
    }

    private final IEventListener<TickEvent> tickUpdate = event -> {
        target = CombatService.getInstance().getTargets()
                .stream()
                .min(Comparator.comparingDouble(entity -> entity.squaredDistanceTo(mc.player)))
                .filter(entity -> {
                    if (targets.getValue().contains(Targets.TEAMS) && mc.player.isTeammate(entity))
                        return false;
                    return true;
                })
                .orElse(null);

        if (target != null) {
            final Box boundingBox = target.getBoundingBox();
            final Vec3d bestAimPoint = RotationUtil.getBestAimPoint(boundingBox);
            final Vector2f rotationTarget = RotationUtil.getRotations(mc.player.getEyePos(), bestAimPoint);

            doVisualBlock = extensions.getValue().contains(Extensions.FAKE_BLOCKING);

            noise.SetNoiseType(FastNoise.NoiseType.CubicFractal);
            noise.SetFractalType(FastNoise.FractalType.FBM);
            noise.SetFrequency(0.07f);
            noise.SetFractalOctaves(3);
            noise.SetFractalLacunarity(2.0f);
            noise.SetFractalGain(0.5f);

            float time = System.nanoTime() / 1_000_000_000f;
            float noiseVal = noise.GetCubicFractal(time, 0);
            float speed = MathHelper.lerp((noiseVal + 1f) / 2f, minRotationSpeed.getValue().floatValue(), maxRotationSpeed.getValue().floatValue());

            RotationService.getInstance().queue(
                    new Rotation(rotationTarget.getX(), rotationTarget.getY()),
                    speed,
                    threshold.getValue().intValue(),
                    ticksTillReset.getValue().floatValue(),
                    Priority.VERY_IMPORTANT,
                    this,
                    extensions.getValue().contains(Extensions.MOVEMENT_FIX),
                    extensions.getValue().contains(Extensions.CHANGE_LOOK)
            );

            if (!facingEnemy(mc.cameraEntity, target, RotationService.getInstance().getServerRotation(), range.getValue()) && extensions.getValue().contains(Extensions.RAYCAST)) {
                return;
            }

            switch (attackMode.getSingleValue()) {
                case HURT_TIME -> {
                    double delay = target.getHealth() < 4 || target.handSwinging || mc.player.hurtTime > 0 ? -1 : 1 / 2.3 * 20 - 1;

                    if (stopwatch.elapsedDouble(delay * 50)) {
                        if (mc.player.getPos().squaredDistanceTo(target.getPos()) <= Math.pow(attackRange.getValue(), 2)) {
                            mc.interactionManager.attackEntity(mc.player, target);
                        }

                        mc.player.swingHand(Hand.MAIN_HAND);
                        stopwatch.reset();
                    }
                }

                case ONE_POINT_NINE -> {
                    if (mc.player.getAttackCooldownProgress(mc.getTickDelta()) >= 1.0f &&
                            mc.player.getPos().squaredDistanceTo(target.getPos()) <= Math.pow(attackRange.getValue(), 2)) {

                        mc.interactionManager.attackEntity(mc.player, target);
                        mc.player.swingHand(Hand.MAIN_HAND);

                        mc.player.resetLastAttackedTicks();
                    }
                }

                case CONSTANT -> {
                    cpsTime += 0.05f;

                    float noiseValue = cpsNoise.GetSimplex(cpsTime, 0);
                    float normalizedNoise = (noiseValue + 1f) / 2f;

                    float currentCPS = MathHelper.lerp(normalizedNoise, minCPS.getValue().floatValue(), maxCPS.getValue().floatValue()) * 1.5f;

                    if (currentClickDelay <= 0) {
                        currentClickDelay = 1000.0 / currentCPS;
                    }

                    if (stopwatch.elapsedDouble(currentClickDelay)) {
                        if (mc.player.getPos().squaredDistanceTo(target.getPos()) <= Math.pow(attackRange.getValue(), 2)) {
                            mc.interactionManager.attackEntity(mc.player, target);
                        }

                        mc.player.swingHand(Hand.MAIN_HAND);
                        stopwatch.reset();

                        float jitter = (float) (Math.random() * 0.2 - 0.1);

                        noiseValue = cpsNoise.GetSimplex(cpsTime + 100, 0);
                        normalizedNoise = (noiseValue + 1f) / 2f;
                        currentCPS = MathHelper.lerp(normalizedNoise, minCPS.getValue().floatValue(), maxCPS.getValue().floatValue());
                        currentCPS *= (1 + jitter);

                        currentClickDelay = 1000.0 / currentCPS;
                    }
                }
            }
        } else {
            doVisualBlock = false;
        }
    };

    private boolean facingEnemy(Entity fromEntity, Entity toEntity, Rotation rotation, double range) {
        Vec3d cameraVec = fromEntity.getCameraPosVec(1.0f);
        Vec3d rotationVec = rotation.getRotationVec();

        double rangeSquared = range * range;

        Vec3d endPos = cameraVec.add(rotationVec.x * range, rotationVec.y * range, rotationVec.z * range);
        Box box = fromEntity.getBoundingBox().stretch(rotationVec.multiply(range)).expand(1.0, 1.0, 1.0);

        EntityHitResult entityHitResult = ProjectileUtil.raycast(
                fromEntity, cameraVec, endPos, box,
                (entity) -> !entity.isSpectator() && entity.canHit() && entity == toEntity,
                rangeSquared
        );

        if (entityHitResult == null) {
            return false;
        }

        double distance = cameraVec.squaredDistanceTo(entityHitResult.getPos());

        return distance <= rangeSquared && canSeePointFrom(cameraVec, entityHitResult.getPos())
                || distance <= rangeSquared;
    }

    private boolean canSeePointFrom(Vec3d eyes, Vec3d vec3) {
        return mc.world.raycast(new RaycastContext(
                eyes, vec3, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player
        )).getType() == HitResult.Type.MISS;
    }

    @Override
    protected void onEnable() {
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        super.onDisable();
    }

    public enum RotationMode {
        LINEAR,
        AI
    }

    private enum ClickMode {
        ONE_POINT_NINE,
        CONSTANT,
        HURT_TIME
    }

    public enum Targets {
        PLAYERS,
        MOBS,
        ANIMALS,
        TEAMS
    }

    private enum Extensions {
        RAYCAST,
        MOVEMENT_FIX,
        CHANGE_LOOK,
        FAKE_BLOCKING
    }
}
