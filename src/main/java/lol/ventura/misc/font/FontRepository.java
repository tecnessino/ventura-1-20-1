package lol.ventura.misc.font;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.Service;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.NoSuchElementException;

public final class FontRepository extends Service implements SimpleSynchronousResourceReloadListener, GameAccessor {
    @Getter @Setter
    private static FontRepository instance;

    private final HashMap<String, IFontRenderer> fonts = new HashMap<>();

    @Getter
    private MinecraftFontRenderer minecraftFontRenderer = null;

    public void init()
    {
        minecraftFontRenderer = new MinecraftFontRenderer();
        fonts.put("minecraft", minecraftFontRenderer);

        try {
            addFonts("roboto", "montserrat", "poppins", "icons", "venturaicons", "nunito", "sf", "geist");
        }catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Cannot initialize fonts");
        }
    }

    private final void addFonts(final String... names) throws IOException {
        Gson gson = new Gson();

        for(String name : names)
        {
            InputStreamReader metaReader = new InputStreamReader(mc.getResourceManager().open(Identifier.of("ventura", "fonts/%s.json".formatted(name))));
            NativeImage image = NativeImage.read(mc.getResourceManager().open(Identifier.of("ventura", "fonts/%s.png".formatted(name))));
            JsonObject object = gson.fromJson(metaReader, JsonObject.class);

            fonts.put(name, new SDFRenderer(object, name, new NativeImageBackedTexture(image)));
        }
    }

    public boolean hasFont(final String name)
    {
        return fonts.containsKey(name);
    }

    public final IFontRenderer getFont(final String name)
    {
        if(!fonts.containsKey(name)) throw new NoSuchElementException("Cannot find this font");
        return fonts.get(name);
    }


    @Override
    public void reload(ResourceManager manager) {
        init();
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of("ventura", "");
    }
}
