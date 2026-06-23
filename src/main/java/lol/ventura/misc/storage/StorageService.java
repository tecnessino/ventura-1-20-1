package lol.ventura.misc.storage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lol.ventura.VenturaClient;
import lol.ventura.foundation.Service;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class StorageService extends Service {

    @Getter @Setter
    private static StorageService instance = null;

    private static final Gson gson = new Gson();
    private JsonObject json;

    public StorageService()
    {
        try {
            json = new JsonObject();

            if(!new File("./ventura.json").exists())
                save();

            json = gson.fromJson(new FileReader("./ventura.json"), JsonObject.class);

            new Thread(() -> {
                while(true)
                {
                    try {
                        Thread.sleep(300000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    save();
                    VenturaClient.getLogger().info("Autosaved storage after 5minutes");
                }
            }).start();

            Runtime.getRuntime().addShutdownHook(new Thread(this::save));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot initialize Storage sevice");
        }
    }

    public void save()
    {
        try {
            new File("./ventura.json").createNewFile();
            FileWriter writer = new FileWriter("./ventura.json");
            writer.write(gson.toJson(json));
            writer.close();
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Cannot save storage");
        }
    }

    public JsonObject getForClass(Class<?> cls)
    {
        if(json.has(cls.getName()))
            return json.get(cls.getName()).getAsJsonObject();

        json.add(cls.getName(), new JsonObject());
        return json.get(cls.getName()).getAsJsonObject();
    }
}
