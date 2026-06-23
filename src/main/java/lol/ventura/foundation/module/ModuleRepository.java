package lol.ventura.foundation.module;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.doubles.DoubleComparator;
import lol.ventura.VenturaClient;
import lol.ventura.features.properties.BooleanProperty;
import lol.ventura.features.properties.EnumProperty;
import lol.ventura.features.properties.MultiProperty;
import lol.ventura.features.properties.NumberProperty;
import lol.ventura.foundation.Service;
import lol.ventura.foundation.property.Property;
import lol.ventura.foundation.property.PropertyRepository;
import lombok.Getter;
import lombok.Setter;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public final class ModuleRepository extends Service {
    @Getter @Setter
    private static ModuleRepository instance = null;

    private final ClassToInstanceMap<Module> moduleRegistry;

    private final Collection<Class<? extends Module>> gatherModules()
    {
        Reflections reflections = new Reflections(VenturaClient.get().getResolver().getModulesPackage());
        return reflections.getSubTypesOf(Module.class);
    }

    public ModuleRepository()
    {
        moduleRegistry = MutableClassToInstanceMap.create();

        Collection<Class<? extends Module>> modules = gatherModules();
        for(var cls : modules)
        {
            if(cls.getName().equals(Module.class.getName()))
                continue;

            try {
                Module m = (Module) cls.getConstructors()[0].newInstance(cls.getAnnotation(ModuleDescriptor.class));
                m.setKey(m.getDescriptor().key());

                VenturaClient.getLogger().info("Initialized {}", m.getDescriptor().name());
                addModules(m);

                if(m.getDescriptor().enableByDefault())
                    m.toggle();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public final <T extends Module> T getModule(final Class<T> cls)
    {
        return moduleRegistry.getInstance(cls);
    }

    public final Collection<Module> getModules()
    {
        return moduleRegistry.values();
    }

    public final Collection<Module> getModulesByCategory(Category category)
    {
        return getModules().stream().filter((module) -> module.getDescriptor().category() == category).collect(Collectors.toList());
    }

    public final void addModules(final Module... modules)
    {
        Arrays.stream(modules).forEach(module -> moduleRegistry.putInstance((Class<Module>) module.getClass(), module));
    }

    public final String generateConfig()
    {
        JsonObject object = new JsonObject();

        for(Module m : getModules())
        {
            JsonObject moduleObj = new JsonObject();
            moduleObj.addProperty("enabled", m.isEnabled());
            moduleObj.addProperty("key", m.getKey());

            for(Property p :  PropertyRepository.getInstance().getPropertiesFromModule(m))
            {
                JsonObject propertyObj = new JsonObject();

                if(p instanceof BooleanProperty)
                {
                    propertyObj.addProperty("value", ((BooleanProperty) p).getValue());
                }

                if(p instanceof NumberProperty)
                {
                    propertyObj.addProperty("value", ((NumberProperty) p).getValue());
                }

                if(p instanceof EnumProperty<?>)
                {
                    propertyObj.addProperty("value", p.getValueAsString());
                }

                if(p instanceof MultiProperty<?>)
                {
                    propertyObj.add("value", ((MultiProperty<?>) p).serialize());
                }

                moduleObj.add(p.getName() + "_property", propertyObj);
            }

            object.add(m.getDescriptor().name(), moduleObj);
        }

        return new Gson().toJson(object);
    }

    public final void loadFromConfig(String config)
    {
        JsonObject object = new Gson().fromJson(config, JsonObject.class);

        for(Module m : getModules())
        {
            if (object.get(m.getDescriptor().name()) == null)
                continue;
            
            JsonObject moduleObj = object.get(m.getDescriptor().name()).getAsJsonObject();

            if(moduleObj.get("enabled").getAsBoolean())
            {
                if(!m.isEnabled())
                    m.toggle();
            }
            m.setKey(moduleObj.get("key").getAsInt());


            for(Property p :  PropertyRepository.getInstance().getPropertiesFromModule(m)) {
                JsonObject propertyObj = moduleObj.getAsJsonObject(p.getName() + "_property");

                if(propertyObj == null)
                    continue;

                if(p instanceof BooleanProperty)
                {
                    p.setValue(propertyObj.get("value").getAsBoolean());
                }

                if(p instanceof NumberProperty)
                {
                    p.setValue(propertyObj.get("value").getAsDouble());
                }

                if(p instanceof EnumProperty<?>)
                {
                    p.setValueFromString(propertyObj.get("value").getAsString());
                }

                if(p instanceof MultiProperty<?>)
                {
                    ((MultiProperty<?>) p).load(propertyObj.getAsJsonArray("value"));
                }
            }
        }
    }
}
