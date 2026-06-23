package lol.ventura.features.modules.render;

import lol.ventura.features.modules.combat.KillAura;
import lol.ventura.features.properties.EnumProperty;
import lol.ventura.features.properties.NumberProperty;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lol.ventura.foundation.module.ModuleRepository;
import lol.ventura.misc.animation.Animation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.*;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.security.PublicKey;

@ModuleDescriptor(name = "Block Animation", category = Category.RENDER, brief = "wykrzywia chuja zeby byla animacja")
public class BlockAnimation extends Module {

    public final EnumProperty<mode> modes = new EnumProperty<>("Mode",mode.LEGACY);
    public final NumberProperty x = new NumberProperty("X",0.0f,-2.0f,2.0f,0.1f);
    public final NumberProperty y = new NumberProperty("Y",0.0f,-2.0f,2.0f,0.1f);
    public final NumberProperty z = new NumberProperty("Z",0.0f,-2.0f,2.0f,0.1f);
    public final NumberProperty scale = new NumberProperty("Scale",1.0f,0.1f,2.0f,0.1f);

    public Animation swing = new Animation();

    public BlockAnimation(ModuleDescriptor descriptor) {
        super(descriptor);
        addSettings(modes,x,y,z,scale);
    }

    public enum mode {
        LEGACY,
        CLICK,
        COOL,
        EXHIBITION_TEMU_EDITION,
        SEX
    }

    private void applySwingOffset(MatrixStack matrices, Arm arm, float swingProgress) {
        int i = arm == Arm.RIGHT ? 1 : -1;
        float f = MathHelper.sin(swingProgress * swingProgress * (float)Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * (45.0F + f * -20.0F)));
        float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float)Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * g * -20.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * -45.0F));
    }

    public float swing2 = 0.01f;
    public int swingDirection = 1;
    public int finishedSwings = 0;

    public void animate(MatrixStack matrices, ItemStack item, float swingProgress, Hand hand, AbstractClientPlayerEntity player)
    {
        swing2 += MinecraftClient.getInstance().getLastFrameDuration() * swingDirection * 0.25f;

        if(swing2 <= 0)
        {
            swingDirection = 1;
            finishedSwings++;
        } else if(swing2 >= 1)
        {
            swingDirection = -1;
        }

        swing.update();
        swing.animate(swing2,0.1f);
        swingProgress = (float) swing.getValue();

        if (isEnabled() && (item.getItem() instanceof SwordItem
                || item.getItem() instanceof AxeItem
                || item.getItem() instanceof PickaxeItem
                || item.getItem() instanceof HoeItem)) {
            matrices.translate(x.getValue(), y.getValue(), z.getValue());
            matrices.scale(scale.getValue().floatValue(), scale.getValue().floatValue(), scale.getValue().floatValue());
            final Arm arm = (hand == Hand.MAIN_HAND) ? player.getMainArm() : player.getMainArm().getOpposite();

            switch(modes.getValue()) {
                case LEGACY:
                    applySwingOffset(matrices, arm, swingProgress);
                    matrices.translate(-0.04f, 0.05f, 0.04142136f);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-275.5f));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(arm.getId() * -51.635f));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(arm.getId() * 106.55f));
                    break;
                case CLICK:
                    applySwingOffset(matrices, arm, swingProgress);
                    break;
                case EXHIBITION_TEMU_EDITION:
                    applySwingOffset(matrices, arm, swingProgress);
                    matrices.translate(-0.04f, 0.05f, 0.04142136f);
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(55.5f));
                    matrices.translate(0.1f, 0.0f, -0.05f);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(arm.getId() * 116.55f));
                    break;
                case COOL:
                    applySwingOffset(matrices, arm, swingProgress);
                    matrices.translate(-0.04f, 0.05f, 0.04142136f);
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(arm.getId() * 70f));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(arm.getId() * -246.55f));
                    matrices.translate(0.0f, 0.0f, 0.0f);
                    break;
                case SEX:
                    //matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((0.5f - swingProgress)*100));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(10));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(110));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-85));
                    matrices.translate(0.3 + -swingProgress * 0.8, swingProgress * 0.5,-0.85f);
                    break;
            }
        }
    }
}