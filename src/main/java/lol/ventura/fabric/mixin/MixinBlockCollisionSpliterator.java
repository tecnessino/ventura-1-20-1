package lol.ventura.fabric.mixin;

import lol.ventura.features.events.CollisionEvent;
import lol.ventura.features.modules.movement.AntiWeb;
import lol.ventura.foundation.event.EventBus;
import lol.ventura.foundation.module.ModuleRepository;
import net.minecraft.block.BlockState;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockCollisionSpliterator.class)
public class MixinBlockCollisionSpliterator {

    @Redirect(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/BlockView;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    public BlockState redirectGetBlockState(BlockView instance, BlockPos blockPos) {
        AntiWeb a = ModuleRepository.getInstance().getModule(AntiWeb.class);
        if (!a.isEnabled()) {
            return instance.getBlockState(blockPos);
        }
        CollisionEvent e = new CollisionEvent(instance.getBlockState(blockPos), blockPos);
        EventBus.getInstance().emit(e);
        return e.getBs();
    }
}

