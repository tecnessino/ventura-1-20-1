package lol.ventura.features.ui;

import lol.ventura.features.modules.render.Interface;
import lol.ventura.features.properties.BooleanProperty;
import lol.ventura.foundation.property.Property;
import lol.ventura.foundation.ui.Effect;
import lol.ventura.foundation.ui.ISimpleEffectRenderer;
import lol.ventura.misc.animation.Animation;
import lol.ventura.misc.font.FontRepository;
import lol.ventura.misc.font.IFontRenderer;
import lol.ventura.misc.math.Stopwatch;
import lol.ventura.misc.render.RenderUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.LivingEntity;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Notify extends Effect {

    @Getter
    private static Notify instance;

    @RequiredArgsConstructor @Getter
    public static enum NotificationSeverity {
        SUCCESS('k', Color.green),
        INFO('m', Color.white),
        WARNING('n', Color.yellow),
        ERROR('l', Color.red);

        private final char icon;
        private final Color color;
    }

    public Notify()
    {
        instance = this;
    }

    @Getter @Setter
    @RequiredArgsConstructor
    public static class Notification {
        private Animation alphaAnimation = new Animation();
        private Animation xAnimation = new Animation();
        private Animation yAnimation = new Animation();
        private boolean easeOut = false;

        private final NotificationSeverity severity;
        private final String title, description;
        private final long duration;

        private Stopwatch timer = null;

        public Color translate(Color color)
        {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alphaAnimation.getValue() *255));
        }

        public void render(IFontRenderer font, ISimpleEffectRenderer renderer, DrawContext context, float inX, float inY)
        {
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

            renderer.drawBackground(context, new Vector2f(x,y),188,32); //add alpha to this soon !


            FontRepository.getInstance().getFont("venturaicons").drawString(String.valueOf(severity.getIcon()), x+10,y+10,16,translate(severity.getColor()));
            RenderUtil.drawPostBloom((ctx) -> {
                FontRepository.getInstance().getFont("venturaicons").drawString(String.valueOf(severity.getIcon()), x+10,y+10,16,translate(severity.getColor()));
            });

            float textY = y + (32*0.5f) - Interface.getFontFixedHalfHeight() - Interface.getFontFixedHalfHeight() - 1;

            font.drawString(title, x+32+5,textY,10,translate(Color.white),context);
            font.drawString(description,x+32+5,textY + (Interface.getFontFixedHalfHeight() * 2) + 1,8,translate(new Color(200,200,200)),context);
        }
    }

    @Override
    public String getName() {
        return "Notify";
    }

    private final BooleanProperty singleNotifyMode = new BooleanProperty("Single mode notify", false);

    @Override
    public List<Property> getProperties() {
        return List.of(singleNotifyMode);
    }

    private ArrayList<Notification> notifications = new ArrayList<>();
    private Notification chatNotification = null;

    public void queue(Notification notification)
    {
        notifications.add(notification);
    }


    @Override
    public Vector2f draw(DrawContext context, int x, int y) {
        IFontRenderer font = Interface.getFont();
        ISimpleEffectRenderer renderer = Interface.getEffectRenderer();

        if(MinecraftClient.getInstance().currentScreen instanceof ChatScreen)
        {
            if(chatNotification == null)
            {
                chatNotification = new Notification(NotificationSeverity.SUCCESS, "Notification", "You can move me!", -1);
                notifications.add(0,chatNotification);
            }
        } else if(chatNotification != null)
        {
            notifications.remove(chatNotification);
            chatNotification = null;
        }

        float offset = y;
        for(Notification notification : notifications)
        {
            if(offset != y && singleNotifyMode.getValue())
            {
                continue;
            }

            if(notification.timer == null)
                notification.timer = new Stopwatch();

            if(notification.duration > 0)
            {
                if(notification.timer.elapsed(notification.duration))
                    notification.setEaseOut(true);
            }

            notification.render(font, renderer, context, x, offset);
            offset -= 32+5;
        }

        try {
            notifications.stream().
                    filter((entry) -> Math.round(entry.getAlphaAnimation().getValue()) == 0 && entry.isEaseOut()).
                    toList().forEach((entry) -> notifications.remove(entry));
        } catch (Exception e) {
            notifications.clear();
        }

        return new Vector2f(200,Math.abs(offset));
    }
}
