package lol.ventura.fabric.mixin;

import lol.ventura.features.modules.render.ESP;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    /*@Inject(method =  "renderEntity", at = @At("HEAD"))
    private void xd(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci)
    {
        if(Nametags.entityFramebuffer != null)
        {
            Nametags.entityFramebuffer.beginWrite(true);
        }
        System.out.println(entity.getName());
    }

    @Inject(method =  "renderEntity", at = @At("RETURN"))
    private void xd2(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci)
    {
        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
    }*/

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BufferBuilderStorage;getEntityVertexConsumers()Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;", shift = At.Shift.BEFORE))
    private void bind(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci)
    {
        ESP.hookBegin();
    }

    @Redirect(method = "renderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    public void sigma(EntityRenderDispatcher instance, Entity entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light)
    {
        MinecraftClient.getInstance().getEntityRenderDispatcher().render(
                entity,
                x,y,z,yaw,tickDelta,matrices,vertexConsumers,light
        );
        ESP.renderEntity(entity, x,y,z,tickDelta,matrices,vertexConsumers);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getEntitySmoothCutout(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;", shift = At.Shift.BY, by = 1))
    private void unbind(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci)
    {
        ESP.hookEnd();
    }
}
