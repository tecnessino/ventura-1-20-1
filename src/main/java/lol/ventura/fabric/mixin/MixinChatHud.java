package lol.ventura.fabric.mixin;

import com.google.common.collect.Lists;
import lol.ventura.features.modules.render.Interface;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatHud.class)
public abstract class MixinChatHud {

    @Shadow
    public abstract int getLineHeight();

    @Final
    @Shadow
    private List<ChatHudLine.Visible> visibleMessages;

    @Final
    @Shadow
    private int scrolledLines;


    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
    private void chat(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci)
    {
        Interface.renderChat(context,currentTick,mouseX,mouseY,getLineHeight(), visibleMessages, scrolledLines);
        ci.cancel();
    }
}
