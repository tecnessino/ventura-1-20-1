package lol.ventura.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import lol.ventura.features.command.ConfigCommand;
import lol.ventura.features.modules.render.AspectRatio;
import lol.ventura.features.modules.render.Interface;
import lol.ventura.features.modules.render.Smooth;
import lol.ventura.foundation.module.ModuleRepository;
import lol.ventura.foundation.rotation.Rotation;
import lol.ventura.foundation.rotation.RotationService;
import lol.ventura.misc.player.RaytracingExtensions;
import lol.ventura.misc.render.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Shadow
    @Final
    private MinecraftClient client;

    @Unique
    public boolean firstRender = true;

    @Shadow
    private float zoom = 1.0F;
    @Shadow
    private float zoomX;
    @Shadow
    private float zoomY;

    @Shadow
    public abstract float getFarPlaneDistance();

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private void onEspRender(float tickDelta, long startTime, boolean tick, CallbackInfo ci)
    {
        final DrawContext drawContext = new DrawContext(client, buffers.getEntityVertexConsumers());
        Interface.begin2D(drawContext);
    }

    @Shadow @Final
    public BufferBuilderStorage buffers;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getOverlay()Lnet/minecraft/client/gui/screen/Overlay;", shift = At.Shift.BEFORE))
    private void onRender(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        final MinecraftClient client = MinecraftClient.getInstance();
        final DrawContext drawContext = new DrawContext(client, buffers.getEntityVertexConsumers());

        if (firstRender) {
            RenderUtil.init();
            ConfigCommand.loadLastConfig();
            firstRender = false;
        }

        if (client.world != null && client.player != null)
            Interface.draw2D(drawContext);

    }

    @Inject(method = "renderWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD))
    private void onRenderWorld(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci, @Local(ordinal = 0) Matrix4f matrix4f2) {
        final Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        final MatrixStack matrixStack2 = new MatrixStack();

        RenderSystem.getModelViewStack().push();
        RenderSystem.getModelViewStack().peek().getPositionMatrix().mul(matrixStack2.peek().getPositionMatrix());

        matrixStack2.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrixStack2.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));

        RenderSystem.applyModelViewMatrix();

        RenderUtil.setModelMatrix(RenderSystem.getModelViewMatrix());
        RenderUtil.setProjectionMatrix(RenderSystem.getProjectionMatrix());
        RenderUtil.setWorldMatrix(matrixStack2.peek().getPositionMatrix());

        RenderSystem.getModelViewStack().pop();
        RenderSystem.applyModelViewMatrix();

        //var matrixSta = new MatrixStack();
        //matrixStack2.multiplyPositionMatrix(matrix4f2);

        //EventBus.getInstance().emit(new Draw3DEvent(matrixSta));
    }

    @Redirect(method = "updateTargetedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;raycast(DFZ)Lnet/minecraft/util/hit/HitResult;"))
    private HitResult hookRaycast(Entity instance, double maxDistance, float tickDelta, boolean includeFluids) {
        if (instance != client.player) return instance.raycast(maxDistance, tickDelta, includeFluids);

        Rotation rotation = (RotationService.getInstance().getCurrentRotation() != null) ?
                RotationService.getInstance().getCurrentRotation() :
                new Rotation(instance.getYaw(tickDelta), instance.getPitch(tickDelta));

        return RaytracingExtensions.raycast(maxDistance, rotation, includeFluids);
    }

    @Inject(at = @At("HEAD"), method = "tiltViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V", cancellable = true)
    public void removeHurtCam(MatrixStack matrixStack_1, float float_1, CallbackInfo ci) {
        if (ModuleRepository.getInstance().getModule(Smooth.class).disableHurtCam()) {
            ci.cancel();
        }
    }

    @Inject(method = "getBasicProjectionMatrix", at = @At("TAIL"), cancellable = true)
    public void getBasicProjectionMatrix(double fov, CallbackInfoReturnable<Matrix4f> cir) {
        AspectRatio mod = ModuleRepository.getInstance().getModule(AspectRatio.class);

        if (mod.isEnabled()) {
            MatrixStack matrixStack = new MatrixStack();
            matrixStack.peek().getPositionMatrix().identity();
            if (zoom != 1.0f) {
                matrixStack.translate(zoomX, -zoomY, 0.0f);
                matrixStack.scale(zoom, zoom, 1.0f);
            }
            matrixStack.peek().getPositionMatrix().mul(new Matrix4f().setPerspective((float) (fov * (double) ((float) Math.PI / 180F)), mod.getAspectScale(), 0.05f, this.getFarPlaneDistance()));
            cir.setReturnValue(matrixStack.peek().getPositionMatrix());
        }
    }
}
