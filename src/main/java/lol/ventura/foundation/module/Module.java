package lol.ventura.foundation.module;

import lol.ventura.features.ui.Notify;
import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.Keyboard;
import lol.ventura.foundation.event.EventBus;
import lol.ventura.foundation.property.Property;
import lol.ventura.foundation.property.PropertyRepository;
import lol.ventura.misc.animation.Animation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Arrays;

@RequiredArgsConstructor
public abstract class Module implements GameAccessor {
    @Getter
    private final ModuleDescriptor descriptor;

    @Getter
    private boolean enabled = false;

    private Keyboard.KeyCombo keyCombo = null;

    @Getter @Setter
    private Animation xAnimation = new Animation(), yAnimation = new Animation();

    @Getter
    private int key;

    @Getter
    private Animation keybindAlphaTranslation = new Animation();

    public void setKey(int key)
    {
        this.key = key;
        registerKeyCombo();
    }

    public void addSettings(final Property... properties)
    {
        PropertyRepository.getInstance().addProperties(this, Arrays.stream(properties).toList());
    }

    protected void onEnable()
    {
        Notify.getInstance().queue(new Notify.Notification(Notify.NotificationSeverity.INFO, getDescriptor().name(), "Enabled " + getDescriptor().name(),5000));
    }

    protected void onDisable()
    {
        Notify.getInstance().queue(new Notify.Notification(Notify.NotificationSeverity.INFO, getDescriptor().name(), "Disabled " + getDescriptor().name(),5000));
    }

    public void registerKeyCombo()
    {
        if(keyCombo != null)
            Keyboard.getInstance().unregister(keyCombo);

        keyCombo = new Keyboard.KeyCombo(new int[]{key}, this::toggle);
        Keyboard.getInstance().register(keyCombo);
    }

    public void toggle()
    {
        enabled = !enabled;
        if(enabled)
        {
            EventBus.getInstance().register(this);
            onEnable();
        }
        else
        {
            EventBus.getInstance().unregister(this);
            onDisable();
        }
    }
}
