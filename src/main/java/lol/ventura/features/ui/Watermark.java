package lol.ventura.features.ui;

import lol.ventura.features.modules.render.Interface;
import lol.ventura.features.properties.NumberProperty;
import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.property.Property;
import lol.ventura.foundation.ui.Effect;
import lol.ventura.foundation.ui.SimpleEffectBuilder;
import lol.ventura.misc.font.IFontRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Vector2f;

import java.awt.*;
import java.util.List;

public class Watermark extends Effect implements GameAccessor {
    private final NumberProperty margin = new NumberProperty("Watermark margins", 1f,0f,5f,0.1f);

    @Override
    public String getName() {
        return "Watermark";
    }

    @Override
    public List<Property> getProperties() {
        return List.of();
    }

    @Override
    public Vector2f draw(DrawContext context, int inX, int inY) {
        IFontRenderer font = Interface.getFont();

        SimpleEffectBuilder builder = new SimpleEffectBuilder(Interface.getMargin(), Interface.getEffectRenderer());
        builder.textBloomGradient(font, "Ventura")
                .splitter().
                text(font, mc.player.getName().getString())
                .splitter()
                .text(font, mc.getCurrentFps() + "fps");

        builder.draw(context, new Vector2f(inX,inY));

        return new Vector2f(builder.getWidth(), 16);
    }
}
