package lol.ventura.foundation.rotation.planner;

import lol.ventura.features.modules.combat.KillAura;
import lol.ventura.foundation.module.ModuleRepository;
import lol.ventura.foundation.rotation.Rotation;
import lol.ventura.foundation.rotation.RotationService;
import lol.ventura.misc.math.FastNoise;
import lol.ventura.misc.math.MathUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.util.math.MathHelper;

import static java.lang.Math.*;

@Data
@AllArgsConstructor
public class AngleSmooth {
    private Float baseTurnSpeed;
    private FastNoise noise = new FastNoise();

    private long lastUpdate;
    private float lastNoiseValue;
    private float accumulatedJitter;

    public AngleSmooth(Float baseTurnSpeed) {
        this.baseTurnSpeed = baseTurnSpeed;

        noise.SetSeed((int) (System.currentTimeMillis() % 10000));
        noise.SetNoiseType(FastNoise.NoiseType.CubicFractal);
        noise.SetFrequency(0.05f);
        noise.SetFractalOctaves(4);
        noise.SetFractalGain(0.4f);
        noise.SetFractalLacunarity(1.8f);
        noise.SetFractalType(FastNoise.FractalType.FBM);

        this.lastUpdate = System.currentTimeMillis();
        this.lastNoiseValue = 0.0f;
        this.accumulatedJitter = 0.0f;
    }

    public Rotation limitAngleChange(Rotation currentRotation, Rotation targetRotation) {
        final KillAura aura = ModuleRepository.getInstance().getModule(KillAura.class);

        switch (aura.getRotationMode().getValue()) {
            case AI:
                return linearAngleChange2(currentRotation, targetRotation);
            default:
                return linearAngleChange(currentRotation, targetRotation);
        }
    }

    private Rotation linearAngleChange(Rotation currentRotation, Rotation targetRotation) {
        final KillAura aura = ModuleRepository.getInstance().getModule(KillAura.class);

        float yawDifference = RotationService.getInstance().angleDifference(targetRotation.getYaw(), currentRotation.getYaw());
        float pitchDifference = RotationService.getInstance().angleDifference(targetRotation.getPitch(), currentRotation.getPitch());

        float baseYawSpeed = (float) MathUtil.ekkoree(aura.getMinRotationSpeed().getValue(), aura.getMaxRotationSpeed().getValue());
        float basePitchSpeed = (float) MathUtil.ekkoree(aura.getMinRotationSpeed().getValue(), aura.getMaxRotationSpeed().getValue());

        double currentTime = System.currentTimeMillis();
        boolean shouldBoost = MathHelper.sin((float) (currentTime / 400.0)) > 0.85;
        float speedMultiplier = shouldBoost ? 1.6f : 1.0f;

        float smoothBoost = shouldBoost ?
                (float) (MathHelper.sin((float) ((currentTime % 400) / 400.0 * MathHelper.PI)) * 0.6 + 1.0) : 1.0f;

        float rotationDifference = (float) hypot(abs(yawDifference), abs(pitchDifference));
        boolean isTargetBehind = abs(yawDifference) > 90.0f;
        float backTargetMultiplier = isTargetBehind ? 1.8f : 1.0f;

        if (isTargetBehind) {
            float smoothBackTurn = (float) (MathHelper.sin((float) (currentTime / 200.0)) * 0.1 + 0.9);
            backTargetMultiplier *= smoothBackTurn;
        }

        float finalYawSpeed = baseYawSpeed * speedMultiplier * smoothBoost * backTargetMultiplier;
        float finalPitchSpeed = basePitchSpeed * speedMultiplier * smoothBoost;

        float microAdjustment = (float) (
                MathHelper.sin((float) (currentTime / 100.0)) * 0.15 +
                        MathHelper.cos((float) (currentTime / 150.0)) * 0.1
        );

        final FastNoise noise = new FastNoise();

        noise.SetSeed((int) (currentTime / 1000));
        noise.SetNoiseType(FastNoise.NoiseType.CubicFractal);
        noise.SetFrequency(0.08f);
        noise.SetFractalOctaves(3);
        noise.SetFractalGain(0.5f);
        noise.SetFractalLacunarity(2.0f);
        noise.SetFractalType(FastNoise.FractalType.FBM);

        float noisyYaw = yawDifference + noise.GetSimplexFractal(yawDifference * 0.1f, pitchDifference * 0.1f) * 2.0f;
        float noisyPitch = pitchDifference + noise.GetSimplexFractal(yawDifference * 0.1f, pitchDifference * 0.1f) * 2.0f;

        float moveYaw = coerceIn(noisyYaw, -finalYawSpeed, finalYawSpeed);
        float movePitch = coerceIn(noisyPitch, -finalPitchSpeed, finalPitchSpeed);

        if (rotationDifference < 10.0f) {
            moveYaw += microAdjustment * 0.3f;
            movePitch += microAdjustment * 0.2f;
        }

        return new Rotation(
                currentRotation.getYaw() + moveYaw,
                currentRotation.getPitch() + movePitch
        );
    }

    private Rotation linearAngleChange2(Rotation currentRotation, Rotation targetRotation) {
        final KillAura aura = ModuleRepository.getInstance().getModule(KillAura.class);
        final long currentTime = System.currentTimeMillis();
        final long deltaTime = currentTime - lastUpdate;
        lastUpdate = currentTime;

        float yawDifference = RotationService.getInstance().angleDifference(targetRotation.getYaw(), currentRotation.getYaw());
        float pitchDifference = RotationService.getInstance().angleDifference(targetRotation.getPitch(), currentRotation.getPitch());

        float minSpeed = aura.getMinRotationSpeed().getValue().floatValue();
        float maxSpeed = aura.getMaxRotationSpeed().getValue().floatValue();

        float baseYawSpeed = (float) MathUtil.ekkoree(minSpeed, maxSpeed);
        float basePitchSpeed = (float) MathUtil.ekkoree(minSpeed * 0.85f, maxSpeed * 0.9f);

        float rotationDistance = (float) hypot(abs(yawDifference), abs(pitchDifference));
        boolean isTargetBehind = abs(yawDifference) > 90.0f;

        float xd = calculateFactors(currentTime, rotationDistance);
        float[] corrected = correctRotation(currentTime, rotationDistance);
        float[] tremor = tremor(currentTime, rotationDistance, deltaTime);

        float finalYawSpeed = baseYawSpeed * xd * (isTargetBehind ? 1.7f : 1.0f);
        float finalPitchSpeed = basePitchSpeed * xd;

        float newNoise = noise.GetSimplexFractal(
                (currentTime % 10000) * 0.001f,
                (yawDifference * 0.01f + pitchDifference * 0.01f)
        );

        float alpha = 0.3f;
        float smoothedNoise = alpha * newNoise + (1 - alpha) * lastNoiseValue;
        lastNoiseValue = smoothedNoise;

        float noisyYaw = yawDifference + smoothedNoise * 1.5f + corrected[0] + tremor[0];
        float noisyPitch = pitchDifference + smoothedNoise * 0.8f + corrected[1] + tremor[1];

        float[] acceleration = accel(rotationDistance, deltaTime);
        noisyYaw *= acceleration[0];
        noisyPitch *= acceleration[1];

        float moveYaw = coerceIn(noisyYaw, -finalYawSpeed, finalYawSpeed);
        float movePitch = coerceIn(noisyPitch, -finalPitchSpeed, finalPitchSpeed);

        if (rotationDistance > 45.0f && abs(yawDifference) > 25.0f) {
            float overshootFactor = 0.1f * MathHelper.sin((rotationDistance - 45.0f) / 45.0f * (float)PI / 2);
            moveYaw *= (1.0f + overshootFactor);
        }

        if (rotationDistance < 5.0f) {
            float focusNoise = noise.GetCubic((currentTime % 10000) * 0.001f, rotationDistance * 0.1f) * 0.3f;
            moveYaw += focusNoise;
            movePitch += focusNoise * 0.5f;
        }

        return new Rotation(
                currentRotation.getYaw() + moveYaw,
                currentRotation.getPitch() + movePitch
        );
    }

    private float calculateFactors(long currentTime, float rotationDistance) {
        float baseModifier = 1.0f + MathHelper.sin((float)(currentTime % 3000) / 3000.0f * (float)PI * 2) * 0.15f;
        float reactionFactor = MathHelper.clamp(1.0f + (rotationDistance / 180.0f) * 0.5f, 0.9f, 1.5f);
        float fatigueFactor = 1.0f - (MathHelper.sin((float)(currentTime % 12000) / 12000.0f * (float)PI) * 0.1f);

        return baseModifier * reactionFactor * fatigueFactor;
    }

    private float[] correctRotation(long currentTime, float rotationDistance) {
        float[] corrections = new float[2];

        if (rotationDistance < 20.0f) {
            float correctionFactor = MathHelper.clamp(1.0f - rotationDistance / 20.0f, 0.0f, 1.0f);
            float yawCorrection = noise.GetCubicFractal((currentTime % 5000) * 0.001f, 0.5f) * correctionFactor;
            float pitchCorrection = noise.GetCubicFractal(0.5f, (currentTime % 5000) * 0.001f) * correctionFactor * 0.7f;

            if (rotationDistance < 3.0f) {
                float microFactor = (3.0f - rotationDistance) / 3.0f;
                yawCorrection *= 1.0f + microFactor * 0.5f;
                pitchCorrection *= 1.0f + microFactor * 0.5f;
            }

            corrections[0] = yawCorrection;
            corrections[1] = pitchCorrection;
        }

        return corrections;
    }

    private float[] tremor(long currentTime, float rotationDistance, long deltaTime) {
        float[] tremor = new float[2];

        float tremorIntensity = MathHelper.clamp(1.5f - rotationDistance / 30.0f, 0.0f, 1.0f) * 0.4f;
        float fastTremor = noise.GetCubic((currentTime % 1000) * 0.01f, (currentTime % 1000) * 0.015f) * tremorIntensity;
        float slowTremor = noise.GetCubic((currentTime % 3000) * 0.002f, (currentTime % 3000) * 0.003f) * tremorIntensity * 1.5f;

        tremor[0] = fastTremor + slowTremor;
        tremor[1] = fastTremor * 0.7f + slowTremor * 0.5f;

        return tremor;
    }

    private float[] accel(float rotationDistance, long deltaTime) {
        float[] acceleration = new float[2];

        float yawAccel = 1.0f;
        float pitchAccel = 1.0f;

        if (rotationDistance > 30.0f) {
            float inertiaFactor = MathHelper.clamp(accumulatedJitter / 50.0f, 0.7f, 1.0f);

            accumulatedJitter += deltaTime * 0.01f;
            if (accumulatedJitter > 100.0f) accumulatedJitter = 100.0f;

            yawAccel = inertiaFactor;
            pitchAccel = inertiaFactor * 0.9f;
        } else {
            accumulatedJitter = max(0, accumulatedJitter - deltaTime * 0.02f);
        }

        acceleration[0] = yawAccel;
        acceleration[1] = pitchAccel;

        return acceleration;
    }

    public float coerceIn(float value, float minValue, float maxValue) {
        return max(minValue, min(value, maxValue));
    }
}