package lol.ventura.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import lol.ventura.features.modules.combat.KillAura;
import lol.ventura.features.modules.render.BlockAnimation;
import lol.ventura.foundation.module.ModuleRepository;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class MixinHeldItemRenderer {
    @Shadow
    public ItemStack mainHand;

    @Shadow
    public float equipProgressMainHand;

    @Shadow
    protected abstract void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress);

    @Shadow @Final public MinecraftClient client;

    public boolean doBlock = false;

    public boolean animateItem(Item item)
    {
        if(item instanceof SwordItem || item instanceof AxeItem || item instanceof PickaxeItem || item instanceof HoeItem)
            return true;

        return false;
    }

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    public void applyViewModel(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        var mod = ModuleRepository.getInstance().getModule(BlockAnimation.class);
        var aura = ModuleRepository.getInstance().getModule(KillAura.class);


        if(mod.isEnabled())
        {
            doBlock = aura.isEnabled() && aura.doVisualBlock && hand == Hand.MAIN_HAND && animateItem(item.getItem());

            matrices.translate(mod.x.getValue(), mod.y.getValue(), mod.z.getValue());
            matrices.scale(mod.scale.getValue().floatValue(), mod.scale.getValue().floatValue(), mod.scale.getValue().floatValue());
        } else doBlock = false;
    }

    @Redirect(method = "renderFirstPersonItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isUsingItem()Z",
            ordinal = 1
    ))
    public boolean isUseItem(AbstractClientPlayerEntity instance) {
        var item = instance.getMainHandStack().getItem();

        if (doBlock) {
            return true;
        }

        return instance.isUsingItem();
    }

    @Redirect(method = "renderFirstPersonItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getActiveHand()Lnet/minecraft/util/Hand;",
            ordinal = 1
    ))
    public Hand getActiveHand(AbstractClientPlayerEntity instance) {
        var item = instance.getMainHandStack().getItem();

        if (doBlock) {
            return Hand.MAIN_HAND;
        }

        return instance.getActiveHand();
    }

    @Inject(method = "renderFirstPersonItem",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getUseAction()Lnet/minecraft/util/UseAction;")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V", ordinal = 2, shift = At.Shift.AFTER))
    public void block(AbstractClientPlayerEntity player, float tickDelta, float pitch,
                                                Hand hand, float swingProgress, ItemStack item, float equipProgress,
                                                MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                                                CallbackInfo ci) {
        if(!doBlock)
            return;

        var mod = ModuleRepository.getInstance().getModule(BlockAnimation.class);
        mod.animate(matrices, item, swingProgress, hand, player);
    }

    @Redirect(method = "renderFirstPersonItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;getUseAction()Lnet/minecraft/util/UseAction;",
            ordinal = 0
    ))
    public UseAction getUseAction(ItemStack instance) {
        if (doBlock) {
            return UseAction.BLOCK;
        }

        return instance.getUseAction();
    }

    @Redirect(method = "renderFirstPersonItem", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;getItemUseTimeLeft()I",
            ordinal = 1
    ))
    public int getItemUseTimeLeft(AbstractClientPlayerEntity instance) {
        var item = instance.getMainHandStack().getItem();

        if (doBlock) {
            return 1;
        }

        return instance.getItemUseTimeLeft();
    }


    @ModifyArg(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V", ordinal = 4), index = 2)
    public float getEquipProgressBlocking(float equipProgress) {
        if (doBlock) {
            return 0.0f;
        }

        return equipProgress;
    }

    @ModifyArg(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V", ordinal = 2), index = 2)
    public float getEquipProgressNone(float equipProgress) {
        if (doBlock) {
            return 0.0f;
        }

        return equipProgress;
    }
}
