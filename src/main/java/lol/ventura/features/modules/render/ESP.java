package lol.ventura.features.modules.render;

import lol.ventura.features.combat.CombatService;
import lol.ventura.features.events.Draw2DEvent;
import lol.ventura.features.events.Draw3DEvent;
import lol.ventura.features.events.TickEvent;
import lol.ventura.features.modules.combat.KillAura;
import lol.ventura.features.ui.Spotify;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lol.ventura.foundation.module.ModuleRepository;
import lol.ventura.misc.animation.Animation;
import lol.ventura.misc.render.FramebufferManager;
import lol.ventura.misc.render.RenderUtil;
import lol.ventura.misc.render.ScaledResolution;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.Collection;

@ModuleDescriptor(name = "ESP", category = Category.RENDER, brief = "patrz przez sciany")
public class ESP extends Module {
    public ESP(ModuleDescriptor descriptor) {
        super(descriptor);
    }

    public static FramebufferManager.FramebufferEntry entityFramebuffer = null;

    private static final Animation progression = new Animation();
    private static int animationDirection = 1;

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    public static void hookBegin() {
        if (!ModuleRepository.getInstance().getModule(ESP.class).isEnabled())
            return;

        if (entityFramebuffer != null) {
            entityFramebuffer.getFramebuffer().clear(true);
            entityFramebuffer.getFramebuffer().beginWrite(true);
        }
    }

    private final IEventListener<Draw3DEvent> event3d = e -> {
        for (LivingEntity entity : CombatService.getInstance().getTargets()) {

            //RenderUtil.getSpiralShader().drawRect(e.getStack().peek().getPositionMatrix(), (float) entity.pos.x, (float) entity.pos.y,100,100);

            /* if(entity == mc.player) continue;


            Vec3d screen = Nametags.convertToScreen(
                    new Vec3d(entity.getPos().x - 0.5f, entity.getPos().y, entity.getPos().z),
                    mc.getEntityRenderDispatcher().camera,
                    mc.getWindow().getHeight(), mc.getWindow().getWidth(),
                    mc.getWindow().getScaleFactor()
            );


            RenderUtil.getSpiralShader().drawRect((float) screen.x, (float) screen.y, (float) (128 * screen.z), (float) (128 * screen.z));*/
        }
    };

    private final IEventListener<Draw2DEvent> event = e -> {
        if (CombatService.getInstance().getTargets().isEmpty())
            return;

        if (KillAura.target == null)
            return;

        Entity entity = KillAura.target;
        RenderUtil.getSpiralShader().drawRect(new Matrix4f().identity(), (float) entity.getPos().x, (float) entity.getPos().y, (float) entity.getPos().z, 128, 128);

        progression.update();
        progression.animate(mc.player.getHeight() * animationDirection, 1f);

        if (Math.round(progression.getValue()) >= mc.player.getHeight()) {
            animationDirection = -1;
        }

        if (0 >= (float) progression.getValue()) {
            animationDirection = 1;
        }
    };

    public static void hookEnd() {
        if (!ModuleRepository.getInstance().getModule(ESP.class).isEnabled())
            return;

        MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
    }

    public static void renderEntity(Entity entity, double x, double y, double z, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        if (ModuleRepository.getInstance().getModule(Tracers.class).isEnabled())
            Tracers.renderEntity(entity,x,y,z,tickDelta,matrices,vertexConsumers);

        if (!ModuleRepository.getInstance().getModule(ESP.class).isEnabled())
            return;


        if (KillAura.target != entity)
            return;

        Color c = new Color(Interface.getTheme().forOffset(0));

        drawRing(
                x,
                y + Spotify.clamp((float) progression.getValue(), 0, 2),
                z,
                1, 1, 180,
                c.getRed() / 255.0f, c.getGreen() / 255.0f,
                c.getBlue() / 255.0f, c.getAlpha() / 255.0f,
                matrices.peek().getPositionMatrix(),
                matrices.peek().getNormalMatrix(),
                vertexConsumers
        );
    }


    public static void drawRing(double x, double y, double z, float innerRadius, float outerRadius, int segments, float red, float green, float blue, float alpha, Matrix4f matrix, Matrix3f normal, VertexConsumerProvider consumerProvider) {

        var buffer = consumerProvider.getBuffer(RenderLayer.getLines());


        for (int i = 0; i <= segments; i++) {
            double angle1 = 2.0 * Math.PI * i / segments;
            double angle2 = 2.0 * Math.PI * (i + 1) / segments;
            float x1_outer = (float) (Math.sin(angle1) * outerRadius);
            float z1_outer = (float) (Math.cos(angle1) * outerRadius);
            float x2_outer = (float) (Math.sin(angle2) * outerRadius);
            float z2_outer = (float) (Math.cos(angle2) * outerRadius);
            float x1_inner = (float) (Math.sin(angle1) * innerRadius);
            float z1_inner = (float) (Math.cos(angle1) * innerRadius);
            float x2_inner = (float) (Math.sin(angle2) * innerRadius);
            float z2_inner = (float) (Math.cos(angle2) * innerRadius);

            buffer.vertex(matrix, (float) (x + x1_outer), (float) y, (float) (z + z1_outer))
                    .color(red, green, blue, alpha)
                    .normal(normal, 0, 1, 0)
                    .next();
            buffer.vertex(matrix, (float) (x + x2_outer), (float) y, (float) (z + z2_outer))
                    .color(red, green, blue, alpha)
                    .normal(normal, 0, 1, 0)
                    .next();

            buffer.vertex(matrix, (float) (x + x1_inner), (float) y, (float) (z + z1_inner))
                    .color(red, green, blue, alpha)
                    .normal(normal, 0, 1, 0)
                    .next();
            buffer.vertex(matrix, (float) (x + x2_inner), (float) y, (float) (z + z2_inner))
                    .color(red, green, blue, alpha)
                    .normal(normal, 0, 1, 0)
                    .next();
        }
    }

    public static void draw() {
        Framebuffer mcFb = MinecraftClient.getInstance().getFramebuffer();
        if (!ModuleRepository.getInstance().getModule(ESP.class).isEnabled())
            return;

        if (entityFramebuffer == null)
            entityFramebuffer = FramebufferManager.getInstance().getFramebuffer(false);

        RenderUtil.drawTexturedRoundedRect(0, 0, ScaledResolution.getWidth(), ScaledResolution.getHeight(), 0, entityFramebuffer.getFramebuffer().getColorAttachment(), true);
    }

    ;


}
