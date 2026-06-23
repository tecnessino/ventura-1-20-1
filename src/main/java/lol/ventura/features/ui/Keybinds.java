package lol.ventura.features.ui;

import lol.ventura.features.modules.render.Interface;
import lol.ventura.features.properties.NumberProperty;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleRepository;
import lol.ventura.foundation.property.Property;
import lol.ventura.foundation.ui.Effect;
import lol.ventura.foundation.ui.SimpleEffectBuilder;
import lol.ventura.misc.font.FontRepository;
import lol.ventura.misc.font.IFontRenderer;
import lol.ventura.misc.render.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Vector2f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Keybinds extends Effect {
    private final NumberProperty margin = new NumberProperty("Keybinds margins", 1f, 0f, 5f, 0.1f);
    @Override
    public String getName() {
        return "Keybinds";
    }

    @Override
    public List<Property> getProperties() {
        return List.of();
    }

    @Override
    public Vector2f draw(DrawContext context, int x, int y) {
        IFontRenderer font = Interface.getFont();
        int effects = (int) ModuleRepository.getInstance().getModules().stream().filter((m) -> m.getKey() != 0 && !m.getDescriptor().name().equalsIgnoreCase("clickui")).count();
        float maxWidth = 0;

        if(effects == 0)
            return new Vector2f(0,0);

        float offset = y;

        SimpleEffectBuilder builder2 = new SimpleEffectBuilder(Interface.getMargin(), Interface.getEffectRenderer());

        builder2.icon(FontRepository.getInstance().getFont("venturaicons"), "c", 8,3);
        builder2.text(font, "Binds");
        builder2.draw(context, new Vector2f(x,offset));
        offset+=16+1;

        for(Module m : ModuleRepository.getInstance().getModules().stream().filter((m) -> m.getKey() != 0).toList())
        {
            SimpleEffectBuilder builder = new SimpleEffectBuilder(Interface.getMargin(), Interface.getEffectRenderer());

            String key = GLFW.glfwGetKeyName(m.getKey(),0);
            if(key == null) continue;

        //    builder.icon(FontRepository.getInstance().getFont("venturaicons"), "c", 8,3);
            builder.text(font, m.getDescriptor().name());
            builder.splitter();
            builder.text(font, " "+  key.toUpperCase(), Color.white);
            builder.draw(context, new Vector2f(x,offset));


            if(maxWidth < builder.getWidth())
                maxWidth = builder.getWidth();

            float splitterSize = Interface.getEffectRenderer().splitterOffset(new SimpleEffectBuilder.MiscInfo(Interface.getMargin()));

            m.getKeybindAlphaTranslation().update();
            m.getKeybindAlphaTranslation().animate(m.isEnabled() ? 188 : 88, 0.05f);

            RenderUtil.drawOutlineRoundedRect(x  + (Interface.getMargin() * 2) + splitterSize + font.getWidth(m.getDescriptor().name(), 8) + (font.getWidth(key.toUpperCase(),8) * 0.5f) - 0.35f,offset+2,12,12,2, new Color(255,255,255, (int) m.getKeybindAlphaTranslation().getValue()),1);
            offset+=16+1;
        }

        return new Vector2f(maxWidth,(effects+1)*17);
    }
}
