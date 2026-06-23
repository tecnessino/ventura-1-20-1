package lol.ventura.features.modules.render;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import lol.ventura.features.events.Draw2DEvent;
import lol.ventura.features.properties.BooleanProperty;
import lol.ventura.features.properties.NumberProperty;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lol.ventura.foundation.module.ModuleRepository;
import lol.ventura.misc.font.FontRepository;
import lol.ventura.misc.font.MinecraftFontRenderer;
import lol.ventura.misc.font.SDFRenderer;
import lol.ventura.misc.render.RenderUtil;
import lol.ventura.misc.render.ScaledResolution;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.*;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ModuleDescriptor(name = "Nametags", category = Category.RENDER, brief = "Oznaczamy kurwy")
public class Nametags extends Module {

    public final BooleanProperty players = new BooleanProperty("Players", true);
    public final BooleanProperty animals = new BooleanProperty("Animals", false);
    private final NumberProperty range = new NumberProperty("Range",100f,10f,1000f,1f);

    public Nametags(ModuleDescriptor descriptor) {
        super(descriptor);
        addSettings(players,animals,range);
    }
    public static Vec3d convertToScreen(Vec3d worldPosition, Camera camera, int displayHeight, int displayWidth, double scaleFactor) {
        Vector3f output = new Vector3f();

        Vec3d relativePosition = worldPosition.subtract(camera.getPos());
        Vector4f worldCoordinates = new Vector4f((float) relativePosition.x, (float) relativePosition.y, (float) relativePosition.z, 1.0f);

        Matrix4f proj = RenderUtil.getProjectionMatrix();

        Matrix4f combinedMatrix = new Matrix4f();
        proj.mul(RenderUtil.getModelMatrix(), combinedMatrix);
        combinedMatrix.mul(RenderUtil.getWorldMatrix());

        Vector4f clipSpace = worldCoordinates.mul(combinedMatrix);

        if (clipSpace.w == 0.0f) {
            return null;
        }

        float ndcX = clipSpace.x / clipSpace.w;
        float ndcY = clipSpace.y / clipSpace.w;

        float screenX = ((ndcX + 1.0f) / 2.0f) * displayWidth / (float) scaleFactor;
        float screenY = ((1.0f - ndcY) / 2.0f) * displayHeight / (float) scaleFactor;

        return new Vec3d(screenX, screenY, clipSpace.z);
    }

    @Override
    protected void onEnable() {
        super.onEnable();
    }

    public static void handle(Entity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci)
    {
        //INFO: MAY CAUSE PROBLEMS, IF SMTH IS NOT RIGHT WITH RENDERING, CHECK THAT FIRST!
        matrices.loadIdentity();
        /*var nametags = ModuleRepository.getInstance().getModule(Nametags.class);

        if(nametags.isEnabled())
        {
            if(entity instanceof LivingEntity && nametags.animals.getValue())
                ci.cancel();

            if(entity instanceof PlayerEntity && nametags.players.getValue())
                ci.cancel();
        }*/
    }

    private final IEventListener<Draw2DEvent> drawEvent = event -> {
        {
            SDFRenderer text = (SDFRenderer) FontRepository.getInstance().getFont("roboto");
            if(!(Interface.getFont() instanceof MinecraftFontRenderer))
            {
                text = (SDFRenderer) Interface.getFont();
            }

            List<LivingEntity> entities = mc.world.getEntitiesByClass(LivingEntity.class, mc.player.getBoundingBox().expand(range.getValue().floatValue()), (entity) -> {
                if(players.getValue() && entity instanceof PlayerEntity) return true;
                return animals.getValue();
            });

            //todo: create position cache.
            //we except here many people (1 person = 1 text).
            // when we have got 1000 people it means 1000 texts and rendering every text in other draw call is unoptimal.
            Multimap<String, org.joml.Vector2f> texts = ArrayListMultimap.create();

            for(LivingEntity entity : entities)
            {
                if(entity == mc.player) continue;


                Vec3d screen = convertToScreen(
                        new Vec3d(entity.getPos().x, entity.getPos().y + entity.getHeight() + 0.5f, entity.getPos().z),
                        mc.getEntityRenderDispatcher().camera,
                        mc.getWindow().getHeight(), mc.getWindow().getWidth(),
                        mc.getWindow().getScaleFactor()
                );

                if(screen == null)
                    continue;

                if(screen.z < 0)
                    continue;

                int itemsNotAir = 0;
                float x = (float) (screen.x - (text.getWidth(entity.getName().getString(), 8) *0.5f));
                if(entity instanceof PlayerEntity)
                {
                    ArrayList<ItemStack> itemy = new ArrayList<>(((PlayerEntity) entity).getInventory().armor);
                    Collections.reverse(itemy);
                    for (ItemStack stack : itemy) {
                        if (stack.getItem() == Items.AIR)
                            continue;

                        itemsNotAir++;
                    }

                    if(itemsNotAir != 0)
                    {
                        float computedWidth = 4 + 4 + (itemsNotAir * (12 + 2)); //padding + padding + (i * (iconSize + offset))

                        x -= computedWidth / 2;

                        RenderUtil.drawBlurRoundedRect(event.getContext().getMatrices(), x-4, (float) screen.y,computedWidth,16,5);

                        float offset = x;
                        for (ItemStack stack : itemy) {
                            if (stack.getItem() == Items.AIR)
                                continue;

                            event.getContext().drawSprite((int) offset, (int) screen.y+2, 0,12,12,MinecraftClient.getInstance()
                                    .getItemRenderer()
                                    .getModel(stack, null, null, 0)
                                    .getParticleSprite());

                            offset += 12+2;
                        }

                        x += computedWidth + 4;
                    }
                }


                RenderUtil.drawBlurRoundedRect(event.getContext().getMatrices(), x-4, (float) screen.y, text.getWidth(entity.getName().getString(), 8)+8,Interface.getFontFixedHalfHeight()*2 + 8,5);
                texts.put(entity.getName().getString(), new Vector2f(x, (float) screen.y + 4));
            }

            text.drawStrings(texts, 8, Color.white);
        }
    };


}