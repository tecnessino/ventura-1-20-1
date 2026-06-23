package lol.ventura.fabric.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import lol.ventura.VenturaClient;
import lol.ventura.features.modules.render.Interface;
import lol.ventura.features.ui.Scoreboard;
import lol.ventura.foundation.module.ModuleRepository;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {
    @Inject(method = "renderHotbar", at = @At(value = "HEAD"), cancellable = true)
    private void hotbar(float tickDelta, DrawContext context, CallbackInfo ci)
    {
        Interface.renderHotbar(tickDelta, context);
        ci.cancel();
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void xdd(DrawContext context, ScoreboardObjective objective, CallbackInfo ci)
    {
        if(!ModuleRepository.getInstance().getModule(Interface.class).enableWidget.isEnabled(Interface.EffectEnum.SCOREBOARD))
            return;
        Scoreboard.objective = objective;
        ci.cancel();
    }
}
