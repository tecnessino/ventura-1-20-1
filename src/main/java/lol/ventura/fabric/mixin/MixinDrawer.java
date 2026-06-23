package lol.ventura.fabric.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderer.class)
public class MixinDrawer {
    @Inject(method = "drawLayer(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)F", at = @At("HEAD"))
    public void xd(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, TextRenderer.TextLayerType layerType, int underlineColor, int light, CallbackInfoReturnable<Float> cir)
    {
        RenderSystem.setShaderColor(1,1,1,1);
    }

    @Inject(method = "drawLayer(Lnet/minecraft/text/OrderedText;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)F", at = @At("HEAD"))
    public void xd(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumerProvider, TextRenderer.TextLayerType layerType, int underlineColor, int light, CallbackInfoReturnable<Float> cir)
    {
        RenderSystem.setShaderColor(1,1,1,1);
    }
}
