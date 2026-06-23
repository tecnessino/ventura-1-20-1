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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.util.math.Vec3d;

import java.math.BigDecimal;
import java.util.List;

public class BPS extends Effect implements GameAccessor {
    private final NumberProperty margin = new NumberProperty("BPS margins", 1f,0f,5f,0.1f);

    @Override
    public String getName() {
        return "BPS";
    }

    @Override
    public List<Property> getProperties() {
        return List.of();
    }

    private Vec3d lastPosition = new Vec3d(0, 0, 0);
    private long lastTime = System.currentTimeMillis();
    private double bps = 0;

    @Override
    public void tick(TickEvent event) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null) {
            long currentTime = System.currentTimeMillis();
            double timeElapsed = (currentTime - lastTime) / 1000.0;
            Vec3d currentPosition = player.getPos();
            double distanceMoved = currentPosition.distanceTo(lastPosition);
            bps = distanceMoved / timeElapsed;

            lastPosition = currentPosition;
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
                .text(font, Math.round(bps * 100.0) / 100.0 + "BPS");

        builder.draw(context, new Vector2f(inX,inY));

        return new Vector2f(builder.getWidth(), 16);
    }
}
