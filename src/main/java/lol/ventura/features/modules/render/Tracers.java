package lol.ventura.features.modules.render;

import lol.ventura.features.properties.BooleanProperty;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.List;

@ModuleDescriptor(name = "Tracers", category = Category.RENDER, brief = "Lines.")
public class Tracers extends Module {
    private static final BooleanProperty players = new BooleanProperty("Players", true);
    private static final BooleanProperty mobs = new BooleanProperty("Mobs", false);
    private static final BooleanProperty animals = new BooleanProperty("Animals", false);

    public Tracers(ModuleDescriptor descriptor) {
        super(descriptor);
        addSettings(players,mobs,animals);
    }

    private static boolean isValid(LivingEntity targetEntity) {
        if (targetEntity == null || !targetEntity.isAlive()) {
            return false;
        }

        if (targetEntity instanceof PlayerEntity player) {
            return players.getValue();
        }
        if (targetEntity instanceof MobEntity) {
            return mobs.getValue();
        }
        if (targetEntity instanceof PassiveEntity) {
            return animals.getValue();
        }

        return false;
    }


    public static void drawLine(Vec3d a, Vec3d b, float red, float green, float blue, float alpha, float red2, float green2, float blue2, float alpha2, Matrix4f matrix, Matrix3f normal, VertexConsumerProvider consumerProvider) {
        var buffer = consumerProvider.getBuffer(RenderLayer.getLines());

        buffer.vertex((float)a.x,  (float)a.y, (float)a.z)
                .color(red, green, blue, alpha)
                .normal(normal, 0, 1, 0)
                .next();
        buffer.vertex(matrix, (float) b.x, (float)b.y, (float)b.z)
                .color(red2, green2, blue2, alpha2)
                .normal(normal, 0, 1, 0)
                .next();
    }

    public static void renderEntity(Entity entity, double x, double y, double z, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers)
    {
        Color c = new Color(Interface.getTheme().forOffset(0));
        Color c2 = new Color(Interface.getTheme().forOffset(999));

        Vec3d correctedA = mc.player.getPos().subtract(mc.getEntityRenderDispatcher().camera.getPos());
        Vec3d correctedB = new Vec3d(x,y+0.5f,z);


        if(entity instanceof LivingEntity && isValid((LivingEntity) entity))
            drawLine(correctedA, correctedB, c.getRed() / 255.0f, c.getGreen() / 255.0f,
                    c.getBlue() / 255.0f, c.getAlpha() / 255.0f, c2.getRed() / 255.0f, c2.getGreen() / 255.0f,
                    c2.getBlue() / 255.0f, c2.getAlpha() / 255.0f, matrices.peek().getPositionMatrix(),
                    matrices.peek().getNormalMatrix(),
                    vertexConsumers);
    }
}
