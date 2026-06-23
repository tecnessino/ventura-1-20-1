package lol.ventura.fabric.mixin;

import lol.ventura.features.events.PlayerAttackEvent;
import lol.ventura.foundation.event.EventBus;
import lol.ventura.foundation.rotation.Rotation;
import lol.ventura.foundation.rotation.RotationService;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ClientPlayerInteractionManager.class)
 public class MixinClientPlayerInteractionManager {
     @Inject(method = "attackEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;syncSelectedSlot()V", shift = At.Shift.AFTER))
     private void hookAttack(PlayerEntity player, Entity target, CallbackInfo callbackInfo) {
         EventBus.getInstance().emit(new PlayerAttackEvent(target));
     }
 
     @ModifyArgs(method = "interactItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/PlayerMoveC2SPacket$Full;<init>(DDDFFZ)V"))
     private void hookFixRotation(Args args) {
         Rotation rotation = RotationService.getInstance().getCurrentRotation();
         if (rotation == null) {
             return;
         }
 
         args.set(3, rotation.getYaw());
         args.set(4, rotation.getPitch());
     }
 }