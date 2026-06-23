package lol.ventura.features.ui;

import lol.ventura.features.combat.CombatService;
import lol.ventura.features.modules.render.Interface;
import lol.ventura.features.modules.render.Nametags;
import lol.ventura.features.properties.BooleanProperty;
import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.property.Property;
import lol.ventura.foundation.ui.Effect;
import lol.ventura.foundation.ui.ISimpleEffectRenderer;
import lol.ventura.misc.animation.Animation;
import lol.ventura.misc.font.IFontRenderer;
import lol.ventura.misc.render.RenderUtil;
import lol.ventura.misc.render.ScaledResolution;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

public class TargetInfo extends Effect implements GameAccessor {
    private HashMap<LivingEntity, TargetInfoInstance> infos = new HashMap<>();
    private final BooleanProperty worldToScreen = new BooleanProperty("3D TargetInfo", false);
    @Override
    public String getName() {
        return "TargetInfo";
    }

    @Override
    public List<Property> getProperties() {
        return List.of(worldToScreen);
    }

    @RequiredArgsConstructor
    public static class TargetInfoInstance {
        public Animation animation = new Animation();
        public Animation alphaAnimation = new Animation();
        public Animation xAnimation = new Animation();
        public Animation yAnimation = new Animation();
        public final LivingEntity entity;
        public boolean easeOut = false;

        public Color translate(Color color)
        {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alphaAnimation.getValue() *255));
        }

        public float getWidth()
        {
            float baseWidth = Interface.getFont().getWidth(entity.getName().getString(), 10)+45;
            float width = baseWidth+65;
            return width;
        }


        public void render(IFontRenderer font, ISimpleEffectRenderer renderer, DrawContext context, float inX, float inY)
        {
            float baseWidth = font.getWidth(entity.getName().getString(), 10)+45;
            float width = baseWidth+65;

            xAnimation.update();
            xAnimation.animate(inX,0.1f);
            yAnimation.update();
            yAnimation.animate(!easeOut ? inY : inY+100,0.1f);

            float x = inX;
            float y = (float) yAnimation.getValue();

            alphaAnimation.update();
            alphaAnimation.animate(easeOut ? 0 : 1, 0.1f);

            if(Math.round(alphaAnimation.getValue()) == 0)
            {
                return;
            }

            float progress = (entity.getHealth() / entity.getMaxHealth()) * baseWidth;
            animation.update();
            animation.animate(progress, 0.1f);

            renderer.drawBackground(context, new Vector2f(x,y),width,48); //add alpha to this soon !

            int glId = 0;
            if(entity instanceof AbstractClientPlayerEntity)
            {
                Identifier id = ((AbstractClientPlayerEntity) entity).getSkinTexture();
                glId = MinecraftClient.getInstance().getTextureManager().getTexture(id).getGlId();
            } else {
                glId = 0;
            }
            RenderUtil.drawHeadRoundedRect(x+6,y+6, 48-6-6,48-6-6, 6, glId);
            font.drawString(entity.getName().getString(), x+6+48, y+10,10,translate(Color.white), context);
            font.drawString(Math.round(entity.getHealth()) + " / " + Math.round(entity.getMaxHealth()), x+6+48,y+22,6,Color.white, context);

            RenderUtil.drawRoundedRect(x+6+48,y+30,baseWidth,4,1,translate(new Color(89,88,88)));
            RenderUtil.drawRoundedRect((float) (x+6+48), (float) (y+30), (float) animation.getValue(),4,1,translate(Color.white));
        }
    }

    @Override
    public Vector2f draw(DrawContext context, int x, int y) {
        List<LivingEntity> targets = new java.util.ArrayList<>(CombatService.getInstance().getTargets().stream().toList());

        if(mc.currentScreen instanceof ChatScreen)
            targets.add(mc.player);

        IFontRenderer font = Interface.getFont();
        ISimpleEffectRenderer renderer = Interface.getEffectRenderer();

        for(LivingEntity entity : targets)
        {
            if(!infos.containsKey(entity)) infos.put(entity, new TargetInfoInstance(entity));
        }

        float maxHeight = ScaledResolution.getHeight() - y;
        int maxHuds = Math.round(maxHeight / (48+5)) - 1;
        float maxWidth = 0;

        float xOffset = x;
        float offset = y;
        int huds = 0;
        for(var entry : infos.entrySet())
        {
            if(worldToScreen.getValue())
            {
                Vec3d screen = Nametags.convertToScreen(
                        new Vec3d(entry.getKey().getPos().x, entry.getKey().getPos().y + (entry.getKey().getHeight() * 0.5f), entry.getKey().getPos().z),
                        mc.getEntityRenderDispatcher().camera,
                        mc.getWindow().getHeight(), mc.getWindow().getWidth(),
                        mc.getWindow().getScaleFactor()
                );
                if(screen == null)
                    continue;

                if(screen.z < 0)
                    continue;

                entry.getValue().render(font, renderer, context, (float) screen.x, (float) screen.y);
            } else {
                entry.getValue().render(font, renderer, context, xOffset, offset);

                if(entry.getValue().getWidth() > maxWidth)
                    maxWidth = entry.getValue().getWidth();

                if(huds >= maxHuds)
                {
                    huds = 0;
                    xOffset += maxWidth+5;
                    offset  =y;
                    maxWidth = 0;
                }

                offset += 48+5;
                huds++;
            }
        }

        try {
            infos.keySet().stream().
                    filter((entity) -> !targets.contains(entity)).toList()
                    .forEach((entity) -> infos.get(entity).easeOut = true);
        }catch (Exception e) {
            infos.clear();
            targets.clear();
        }

        try {
            infos.entrySet().stream().
                    filter((entry) -> Math.round(entry.getValue().alphaAnimation.getValue()) == 0 && entry.getValue().easeOut).
                    toList().forEach((entry) -> infos.remove(entry.getKey()));
        } catch (Exception e) {
            infos.clear();
            targets.clear();
        }

        return new Vector2f(xOffset,offset);
    }
}
