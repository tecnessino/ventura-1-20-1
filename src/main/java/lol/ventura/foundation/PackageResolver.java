package lol.ventura.foundation;

import lol.ventura.VenturaClient;
import lol.ventura.features.modules.render.Interface;

public final class PackageResolver {
    private final String BASE_PACKAGE = "lol.ventura.%s";

    public final String getModulesPackage() {
        if (VenturaClient.IS_DEVELOPMENT)
            return BASE_PACKAGE.formatted("features.modules");
        else
            return Interface.class.getPackageName();
    }
}
