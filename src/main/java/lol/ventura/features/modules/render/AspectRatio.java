package lol.ventura.features.modules.render;

import lol.ventura.features.properties.BooleanProperty;
import lol.ventura.features.properties.MultiProperty;
import lol.ventura.features.properties.NumberProperty;
import lol.ventura.foundation.module.Category;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleDescriptor;
import lombok.AllArgsConstructor;

@ModuleDescriptor(
        name = "Aspect Ratio",
        category = Category.RENDER,
        brief = "csgo 4:3 ale 16:9 ahh",
        key = 0
)
public class AspectRatio extends Module {

    public final NumberProperty scale = new NumberProperty("Custom Scale", 1.9f, 0.1f, 5.0f, 0.1f);

    @AllArgsConstructor
    enum Proportions
    {
        P_16_9(16f / 9f),
        P_16_10(16f / 10f),
        P_21_9(21f / 9f),
        P_4_3(4f / 3f);

        final float ratio;
    }


    public final MultiProperty<Proportions> proportions  = new MultiProperty<Proportions>("Proportions", false, Proportions.P_16_9);
    public AspectRatio(ModuleDescriptor descriptor) {
        super(descriptor);
        addSettings(proportions);
    }

    public float getAspectScale() {
        if(proportions.getSingleValue() != null)
            return proportions.getSingleValue().ratio;

        return scale.getValue().floatValue();
    }
}