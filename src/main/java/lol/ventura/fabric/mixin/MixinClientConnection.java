package lol.ventura.fabric.mixin;

import io.netty.channel.ChannelHandlerContext;
import lol.ventura.features.events.NetworkPacketEvent;
import lol.ventura.foundation.event.EventBus;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class MixinClientConnection {
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void injectOutgoingPacketEvent(Packet<?> packet, CallbackInfo ci) {
        final NetworkPacketEvent event = new NetworkPacketEvent(packet, NetworkPacketEvent.Type.SENT, false);
        EventBus.getInstance().emit(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/ClientConnection;handlePacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;)V",
            ordinal = 0),
            cancellable = true)
    private void injectIncomingPacketEvent(final ChannelHandlerContext channelHandlerContext, Packet<?> packet, final CallbackInfo ci) {
        final NetworkPacketEvent event = new NetworkPacketEvent(packet, NetworkPacketEvent.Type.RECEIVED, false);
        EventBus.getInstance().emit(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

}