package lol.ventura.features.modules.render;

import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import org.lwjgl.glfw.GLFW;

@ModuleDescriptor(name = "ClickUI", category = Category.RENDER, brief = "click gui", key = GLFW.GLFW_KEY_RIGHT_SHIFT)
public class ClickUI extends Module {
    private final lol.ventura.features.screen.NewClickUI clickUI;
    public ClickUI(ModuleDescriptor descriptor) {
        super(descriptor);
        clickUI = new lol.ventura.features.screen.NewClickUI();
    }

    @Override
    protected void onEnable() {
        toggle();
        mc.setScreen(clickUI);
    }

    @Override
    protected void onDisable() {

    }
}
