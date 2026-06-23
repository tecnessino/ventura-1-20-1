package lol.ventura.fabric.mixin;

import lol.ventura.features.modules.render.Nametags;
import lol.ventura.foundation.module.ModuleRepository;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer<T extends Entity>  {
    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    public void sigma(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci)
    {   
        Nametags.handle(entity,text,matrices,vertexConsumers,light,ci);
    }
}
