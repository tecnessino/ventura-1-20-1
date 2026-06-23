package lol.ventura.features.modules.movement;

import lol.ventura.features.events.CollisionEvent;
import lol.ventura.features.events.TickEvent;
import lol.ventura.features.properties.EnumProperty;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lol.ventura.misc.player.InventoryUtil;
import net.minecraft.block.Blocks;
import net.minecraft.block.CobwebBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@ModuleDescriptor(name = "AntiWeb", category = Category.MOVEMENT, brief = "cwel")
public class AntiWeb extends Module {

    public final EnumProperty<Mode> mode = new EnumProperty<>("Mode", Mode.SOLID);

    public AntiWeb(ModuleDescriptor descriptor) {
        super(descriptor);
        addSettings(mode);
    }

    private final IEventListener<TickEvent> onTick = e -> {
    };

    private final IEventListener<CollisionEvent> onCollision = e -> {
        if (e.getBs().getBlock() instanceof CobwebBlock && mode.getValue() == Mode.SOLID) {
            e.setBs(Blocks.STONE.getDefaultState());
        }
    };

    public enum Mode {
        SOLID,
        WATER
    }
}
