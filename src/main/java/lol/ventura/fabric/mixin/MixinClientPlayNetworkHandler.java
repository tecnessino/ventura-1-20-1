package lol.ventura.fabric.mixin;

import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.command.Command;
import lol.ventura.foundation.command.CommandRepository;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "sendChatMessage(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void sendChatMessage(String message, CallbackInfo ci) {
        CommandRepository cr = CommandRepository.getInstance();
        if (cr == null) return;

        String prefix = cr.getPrefix();
        if (message.startsWith(prefix)) {
            ci.cancel();

            String[] parts = message.substring(prefix.length()).split("\\s+");
            if (parts.length == 0) return;

            String commandName = parts[0];
            String[] args = Arrays.copyOfRange(parts, 1, parts.length);

            for (Command command : cr.getCommands()) {
                for (String usage : command.getUsages()) {
                    if (commandName.equalsIgnoreCase(usage)) {
                        command.execute(args);
                        return;
                    }
                }
            }
            GameAccessor.sendChatMessage("Command not found: " + commandName);
        }
    }
}