package lol.ventura.fabric.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lol.ventura.foundation.rotation.Rotation;
import lol.ventura.foundation.rotation.RotationService;
import lol.ventura.foundation.rotation.planner.RotationPlan;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer<T extends LivingEntity> {
    @Unique
    private final ThreadLocal<Pair<Float, Float>> rotationPitch = ThreadLocal.withInitial(() -> null);

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    private void injectRender(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        GlStateManager._disableDepthTest();

        RotationService rotationService = RotationService.getInstance();
        Rotation rotation = rotationService.getCurrentRotation();
        RotationPlan plan = rotationService.getStoredRotationPlan();
        Pair<Float, Float> rotationPitch = RotationService.getInstance().getRotationPitch();

        this.rotationPitch.remove();

        if (livingEntity != MinecraftClient.getInstance().player || (rotation == null || plan == null)) {
            return;
        }

        this.rotationPitch.set(new Pair<>(rotationPitch.getLeft(), rotationPitch.getRight()));
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("RETURN"))
    private void injectRender2(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        GlStateManager._enableDepthTest();
    }

    @Redirect(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F", ordinal = 0))
    private float injectRotationPitch(float g, float f, float s) {
        Pair<Float, Float> rot = this.rotationPitch.get();

        if (rot != null) {
            return MathHelper.lerp(g, rot.getLeft(), rot.getRight());
        } else {
            return MathHelper.lerp(g, f, s);
        }
    }
}