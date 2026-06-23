package lol.ventura.features.modules.render;

import lol.ventura.features.properties.BooleanProperty;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;

@ModuleDescriptor(name = "Smooth", category = Category.RENDER, brief = "Jajo")
public class Smooth extends Module {
    public Smooth(ModuleDescriptor descriptor) {
        super(descriptor);
    }

    private final BooleanProperty noHurtCam = new BooleanProperty("Disable hurt cam", true);

    public boolean prevViewBobbing = false;
    public int prevEffectInfluence = 0;

    @Override
    protected void onEnable() {
        super.onEnable();
        prevEffectInfluence = mc.options.getFovEffectScale().getValue().intValue();
        prevViewBobbing = mc.options.getBobView().getValue();

        mc.options.getFovEffectScale().setValue(0d);
        mc.options.getBobView().setValue(false);
    }

    public boolean disableHurtCam()
    {
        return isEnabled() && noHurtCam.getValue();
    }

    @Override
    protected void onDisable() {
        super.onDisable();

        mc.options.getFovEffectScale().setValue((double) prevEffectInfluence);
        mc.options.getBobView().setValue(prevViewBobbing);
    }
}
