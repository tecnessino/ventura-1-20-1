package lol.ventura.features.modules.player;

import lol.ventura.features.properties.BooleanProperty;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lombok.Getter;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.Box;

@ModuleDescriptor(name = "Safe Walk", category = Category.PLAYER, brief = "bezpieczne chodzenie")
public class SafeWalk extends Module {
    public SafeWalk(ModuleDescriptor descriptor) {
        super(descriptor);
    }

    @Getter
    private final BooleanProperty fastPlace = new BooleanProperty("Fast Place", true);

    private boolean sneaking;
    public float edgeDistance = 0;

    public void onClipAtLedge(boolean clipping)
    {
        edgeDistance = -0.05f;
        ClientPlayerEntity player = mc.player;


        if(!isEnabled() || !player.isOnGround())
        {
            if(sneaking)
                setSneaking(false);

            return;
        }

        Box box = player.getBoundingBox();
        Box adjustedBox = box.stretch(0, -player.getStepHeight(), 0)
                .expand(edgeDistance, 0, edgeDistance);

        if(mc.world.isSpaceEmpty(player, adjustedBox))
            clipping = true;

        setSneaking(clipping);
    }

    private void setSneaking(boolean sneaking)
    {
        KeyBinding sneakKey = mc.options.sneakKey;;

       /* if(sneaking)
            sneakKey.setPressed(true);
        else
            sneakKey.setPressed(false);*/

        if(sneaking)
        {
            mc.player.input.movementSideways = 0;
            mc.player.input.movementForward = -1;
        }

        this.sneaking = sneaking;
    }
}
