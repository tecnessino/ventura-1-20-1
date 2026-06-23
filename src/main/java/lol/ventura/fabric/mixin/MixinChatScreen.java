package lol.ventura.fabric.mixin;

import lol.ventura.features.modules.render.Interface;
import lol.ventura.foundation.module.ModuleRepository;
import lol.ventura.foundation.ui.Effect;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.Vector2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract  class MixinChatScreen {
    public Effect draggedEffect = null;
    public Vector2f offset = null;

    @Inject(method = "render", at= @At("HEAD"))
    private void draw(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci)
    {
        Interface ui = ModuleRepository.getInstance().getModule(Interface.class);

        if(ui.leftIsDown)
            draggedEffect = null;

        if(draggedEffect != null)
        {
            draggedEffect.bounds.setX(mouseX - offset.getX());
            draggedEffect.bounds.setY(mouseY - offset.getY());
        }

        ui.drawEdges(context.getMatrices());
    }


    @Inject(method = "mouseClicked", at=@At("HEAD"))
    private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir)
    {
        Interface ui = ModuleRepository.getInstance().getModule(Interface.class);
        for(Effect effects : ui.getEffects())
        {
            if(mouseX >= effects.bounds.getX()
                    && mouseY >= effects.bounds.getY() &&
                    mouseX <= effects.bounds.getX() + effects.bounds.getWidth()
                    && mouseY <= effects.bounds.getY() + effects.bounds.getHeight())
            {
                draggedEffect = effects;
                offset = new Vector2f((float) (mouseX - effects.bounds.getX()), (float) (mouseY - effects.bounds.getY()));
            }
        }
    }

}
