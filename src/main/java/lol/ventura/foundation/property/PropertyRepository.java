package lol.ventura.foundation.property;

import com.google.common.collect.*;
import lol.ventura.foundation.Service;
import lol.ventura.foundation.module.Module;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;

public class PropertyRepository extends Service {
    @Getter @Setter
    private static PropertyRepository instance;

    private final ListMultimap<Module, Property> properties = ArrayListMultimap.create();

    public void addProperties(Module m, Collection<Property> properties)
    {
        this.properties.putAll(m, properties);
    }

    public Collection<Property> getPropertiesFromModule(Module m)
    {
        return properties.get(m);
    }
}
