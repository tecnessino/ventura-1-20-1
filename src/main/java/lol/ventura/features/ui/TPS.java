package lol.ventura.features.ui;

import lol.ventura.features.events.TickEvent;
import lol.ventura.features.modules.render.Interface;
import lol.ventura.features.properties.NumberProperty;
import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.property.Property;
import lol.ventura.foundation.ui.Effect;
import lol.ventura.foundation.ui.SimpleEffectBuilder;
import lol.ventura.misc.font.FontRepository;
import lol.ventura.misc.font.IFontRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Vector2f;

import java.util.List;

public class TPS extends Effect implements GameAccessor {
    private final NumberProperty margin = new NumberProperty("TPS margins", 1f, 0f, 5f, 0.1f);

    @Override
    public String getName() {
        return "TPS";
    }

    @Override
    public List<Property> getProperties() {
        return List.of();
    }

    private int tickCount = 0;
    private long lastTime = System.currentTimeMillis();
    private double tps = 20.0;

    @Override
    public void tick(TickEvent event) {
        tickCount++;
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastTime >= 1000) {
            tps = tickCount / ((currentTime - lastTime) / 1000.0);
            if (tps > 20.0) tps = 20.0;

            tickCount = 0;
            lastTime = currentTime;
        }
    }

    @Override
    public Vector2f draw(DrawContext context, int inX, int inY) {
        IFontRenderer font = Interface.getFont();

        SimpleEffectBuilder builder = new SimpleEffectBuilder(Interface.getMargin(), Interface.getEffectRenderer());
        builder
                .icon(FontRepository.getInstance().getFont("venturaicons"), "h", 8, 3)
                .splitter()
                .text(font, Math.round(tps * 100.0) / 100.0 + " TPS");

        builder.draw(context, new Vector2f(inX, inY));

        return new Vector2f(builder.getWidth(), 16);
    }
}
