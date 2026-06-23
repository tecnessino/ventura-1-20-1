package lol.ventura.fabric.mixin;

import lol.ventura.misc.animation.Animation;
import lol.ventura.misc.render.ScaledResolution;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(HandledScreen.class)
public class MixinHandledScreen extends Screen{
    protected MixinHandledScreen(Text title) {
        super(title);
    }

    @Shadow
    private int backgroundWidth;

    @Shadow
    private int backgroundHeight;

    private static HashMap<Screen, Animation> animationHashMap = new HashMap<>();

    @Inject(method = "render", at = @At("HEAD"))
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci)
    {
        var sigma = (Screen) this;

        if(!animationHashMap.containsKey(sigma))
            animationHashMap.put(sigma, new Animation());

        Animation a = animationHashMap.get(sigma);
        a.update();
        a.animate((ScaledResolution.getHeight() * 0.5f) - (backgroundHeight * 0.5f), 0.1f);

        context.getMatrices().translate(0,((ScaledResolution.getHeight() * 0.5f) - (backgroundHeight * 0.5f)) - a.getValue(),0);
    }

}
