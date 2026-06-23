package lol.ventura;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import lol.ventura.fabric.Mod;
import lol.ventura.foundation.PackageResolver;
import lol.ventura.foundation.Service;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.NoSuchElementException;


public final class VenturaClient {
    private static VenturaClient instance;
    private ClassToInstanceMap<Service> services;

    public static final boolean IS_DEVELOPMENT = true;

    @Getter
    private static final Logger logger = LoggerFactory.getLogger(Mod.MOD_ID);

    @Getter
    private final PackageResolver resolver = new PackageResolver();

    private VenturaClient()
    {
        if (instance != null) throw new IllegalStateException("Ventura is already initialized.");
        instance = this;
    }

    public static VenturaClient get()
    {
        if (instance == null) throw new IllegalStateException("Ventura is not initialized");
        return instance;
    }

    public <T extends Service> T getService(final Class<T> serviceClass)
    {
        return this.services.getInstance(serviceClass);
    }

    @SafeVarargs
    public static VenturaClient create(final Class<? extends Service>... services) throws InstantiationException, IllegalAccessException {
        final ClassToInstanceMap<Service> serviceList = MutableClassToInstanceMap.create();
        final VenturaClient client = new VenturaClient();

        for (final Class<? extends Service> cls : services) {
            Service service = cls.newInstance();

            try {
                Arrays.stream(service.getClass().getDeclaredMethods()).
                        filter((method) -> method.getName().equalsIgnoreCase("setInstance")).
                        findFirst().orElseThrow().
                        invoke(null, service);

            } catch (NoSuchElementException e) {
                throw new IllegalStateException("Service hasn't got instance field.");
            }
            catch (InvocationTargetException | IllegalAccessException e) {
                throw new IllegalStateException("Service's instance field has wrong access parameters");
            }

            serviceList.putInstance((Class<Service>)service.getClass(), service);
        }

        client.services = serviceList;
        return client;
    }
}
