package lol.ventura.features.modules.render;

import lol.ventura.features.events.Draw2DEvent;
import lol.ventura.features.ui.Spotify;
import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.event.IEventListener;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lol.ventura.foundation.module.ModuleRepository;
import lol.ventura.foundation.themes.Themes;
import lol.ventura.misc.math.Stopwatch;

import java.util.List;
import java.util.RandomAccess;
import java.util.concurrent.ThreadLocalRandom;

@ModuleDescriptor(name = "Lens", category = Category.RENDER, brief = "xd")
public class Trailer extends Module {
    public Trailer(ModuleDescriptor descriptor) {
        super(descriptor);
    }

    public Stopwatch stopwatch = new Stopwatch();

    private final IEventListener<Draw2DEvent> ev = e -> {
        if(stopwatch.elapsed(100))
        {
            Interface hud = ModuleRepository.getInstance().getModule(Interface.class);
            Themes[] themes = Themes.values();
            Interface.WidgetStyle[] styles = Interface.WidgetStyle.values();

            int c = (int) Spotify.clamp(ThreadLocalRandom.current().nextInt(themes.length), 0, 999);
            int b = (int) Spotify.clamp(ThreadLocalRandom.current().nextInt(styles.length), 0, 999);


            hud.theme.setValue(List.of(themes[c]));
            hud.widgetStyle.setValue(List.of(styles[b]));
            //GameAccessor.sendChatMessage("randomize");
            stopwatch.reset();
        }
    };
}
