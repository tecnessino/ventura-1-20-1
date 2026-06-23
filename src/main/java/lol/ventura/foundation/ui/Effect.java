package lol.ventura.foundation.ui;

import lol.ventura.features.events.TickEvent;
import lol.ventura.features.modules.render.Interface;
import lol.ventura.features.properties.BooleanProperty;
import lol.ventura.foundation.property.Property;
import lol.ventura.misc.math.Bounds;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Vector2f;

import java.util.List;

public abstract class Effect {
    public Bounds bounds = null;
    public boolean enabled = false;
    public Interface.EffectEnum state = null;

    public abstract String getName();
    public abstract List<Property> getProperties();
    public abstract Vector2f draw(DrawContext context, int x, int y);
    public void tick(TickEvent event)
    {

    }
}
