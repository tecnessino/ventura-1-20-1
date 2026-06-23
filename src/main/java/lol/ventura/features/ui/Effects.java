package lol.ventura.features.ui;

import lol.ventura.features.modules.render.Interface;
import lol.ventura.features.properties.NumberProperty;
import lol.ventura.foundation.property.Property;
import lol.ventura.foundation.ui.Effect;
import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.ui.SimpleEffectBuilder;
import lol.ventura.misc.font.FontRepository;
import lol.ventura.misc.font.IFontRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.client.texture.Sprite;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class Effects extends Effect implements GameAccessor {
    private final NumberProperty margin = new NumberProperty("Effects margins", 1f, 0f, 5f, 0.1f);

    @Override
    public String getName() {
        return "Effects";
    }

    @Override
    public List<Property> getProperties() {
        return List.of();
    }

    @Override
    public Vector2f draw(DrawContext context, int inX, int inY) {
        IFontRenderer font = Interface.getFont();
        int yOffset = 0;
        float maxWidth = 0;

        if(mc.player.getStatusEffects().isEmpty())
            return new Vector2f(0,0);

        SimpleEffectBuilder builder2 = new SimpleEffectBuilder(Interface.getMargin(), Interface.getEffectRenderer());

        builder2.icon(FontRepository.getInstance().getFont("venturaicons"), "i", 8,3);
        builder2.text(font, "Potions");
        builder2.draw(context,new Vector2f(inX,inY));
        yOffset += 16+1;

        if (mc.player != null) {
            List<StatusEffectInstance> sortedEffects = mc.player.getStatusEffects().stream()
                    .sorted(Comparator
                            .comparingInt((StatusEffectInstance effect) -> {
                                int durationDigits = String.valueOf(effect.getDuration() / 20).length();
                                int nameLength = effect.getEffectType().getName().getString().length();
                                return -(durationDigits + nameLength);
                            })
                    )
                    .toList();

            for (StatusEffectInstance effect : sortedEffects) {
                StatusEffect type = effect.getEffectType();

                Sprite sprite = mc.getStatusEffectSpriteManager().getSprite(type);


                SimpleEffectBuilder builder = new SimpleEffectBuilder(Interface.getMargin(), Interface.getEffectRenderer());

                String effectName = type.getName().getString();
                String timeLeft = formatTime(effect.getDuration());

                builder.sprite(sprite)
                        .text(font, " " + effectName, Color.WHITE);

                if(!effect.isInfinite())
                {
                    builder.splitter()
                        .text(font, timeLeft, new Color(137, 107, 232));
                }

                Vector2f textPosition = new Vector2f(inX, inY + yOffset);
                builder.draw(context, textPosition);

                float totalWidth = 18 + builder.getWidth();
                if (totalWidth > maxWidth) maxWidth = totalWidth;

                yOffset += 17;
            }
        }

        return new Vector2f(maxWidth, yOffset);
    }

    private String formatTime(int ticks) {
        if (ticks <= 0) return "∞";

        int seconds = (ticks / 20) % 60;
        int minutes = (ticks / 1200);
        return String.format("%02d:%02d", minutes, seconds);
    }
}