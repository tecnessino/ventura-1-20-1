package lol.ventura.fabric.mixin;

import lol.ventura.features.modules.render.BlockAnimation;
import lol.ventura.foundation.module.ModuleRepository;
import lol.ventura.foundation.rotation.Rotation;
import lol.ventura.foundation.rotation.RotationService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class MixinItem {
    @Redirect(method = "raycast", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getYaw()F"))
    private static float hookFixRotationA(PlayerEntity instance) {
        Rotation rotation = RotationService.getInstance().getCurrentRotation();
        if (instance != MinecraftClient.getInstance().player || rotation == null) {
            return instance.getYaw();
        }

        return rotation.getYaw();
    }

    @Redirect(method = "raycast", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getPitch()F"))
    private static float hookFixRotationB(PlayerEntity instance) {
        Rotation rotation = RotationService.getInstance().getCurrentRotation();
        if (instance != MinecraftClient.getInstance().player || rotation == null) {
            return instance.getPitch();
        }

        return rotation.getPitch();
    }

}