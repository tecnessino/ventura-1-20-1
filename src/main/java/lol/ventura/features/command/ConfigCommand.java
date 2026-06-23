package lol.ventura.features.command;

import com.google.gson.JsonObject;
import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.command.Command;
import lol.ventura.foundation.module.ModuleRepository;
import lol.ventura.misc.storage.StorageService;
import lol.ventura.features.modules.render.Interface;
import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.command.Command;
import lol.ventura.foundation.module.ModuleRepository;
import net.minecraft.util.Formatting;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigCommand extends Command {
    private static final Path CONFIG_DIR = Paths.get("Ventura/Configs/");
    public static String currentLoadedConfig = "";

    //TODO: reconstruct systems to configservice when online cfg.

    public ConfigCommand() {
        super("config",
                "config",
                "config save <name>",
                "config load <name>",
                "config list",
                "config delete <name>"
        );
    }

    public static void loadLastConfig() {
        try {
            JsonObject storage = StorageService.getInstance().getForClass(ConfigCommand.class);
            Path p = Path.of(storage.get("lastConfigPath").getAsString());
            String configData = new String(Files.readAllBytes(p));

            currentLoadedConfig = FilenameUtils.removeExtension(String.valueOf(p.getFileName()));

            ModuleRepository.getInstance().loadFromConfig(configData);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    @Override
    public void execute(String... args) {
        if (args.length == 0) {
            sendUsage();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "save":
                handleSave(args);
                break;
            case "load":
                handleLoad(args, false);
                break;
            case "list":
                handleList();
                break;
            case "delete":
                handleDelete(args, false);
                break;
            default:
                sendError("Invalid Operation: " + args[0]);
        }
    }

    private void handleSave(String[] args) {
        if (args.length < 2) {
            sendError("Input config name!");
            return;
        }

        try {
            String configName = validateFileName(args[1]);
            String configData = ModuleRepository.getInstance().generateConfig();
            Path configPath = CONFIG_DIR.resolve(configName + ".vent");

            Files.createDirectories(CONFIG_DIR);
            Files.write(configPath, configData.getBytes());

            GameAccessor.sendChatMessage(Formatting.GRAY + "Config " + Interface.getTheme().getChatPrefixColor() + "'" + configName + "'" + Formatting.GRAY + " has been saved!");
        } catch (Exception e) {
            sendError("Saving failed: " + e.getMessage());
        }
    }

    public static void handleLoad(String[] args, boolean silent) {
        if (args.length < 2) {
            sendError("Input name of your config!");
            return;
        }

        try {
            String configName = validateFileName(args[1]);
            Path configPath = CONFIG_DIR.resolve(configName + ".vent");

            currentLoadedConfig = configName;

            if (!Files.exists(configPath)) {
                if (!silent)
                    sendError("Config " + Formatting.YELLOW + configName + Formatting.RED + " doesn't exist!");
                return;
            }

            JsonObject storage = StorageService.getInstance().getForClass(ConfigCommand.class);
            storage.addProperty("lastConfigPath", configPath.toAbsolutePath().toString());
            StorageService.getInstance().save();

            String configData = new String(Files.readAllBytes(configPath));
            ModuleRepository.getInstance().loadFromConfig(configData);
            if (!silent)
                GameAccessor.sendChatMessage(Formatting.GRAY + "Config " + Interface.getTheme().getChatPrefixColor() + "'" + configName + "'" + Formatting.GRAY + " has been loaded!");
        } catch (Exception e) {
            if (!silent) sendError("Loading error: " + e.getMessage());
            else e.printStackTrace();
        }
    }


    public static Set<String> getConfigList() throws IOException {
        return Files.list(CONFIG_DIR)
                .filter(p -> p.toString().endsWith(".vent"))
                .map(p -> p.getFileName().toString().replace(".vent", "")).collect(Collectors.toSet());
    }

    public static boolean delete(String name) throws IOException {
        String configName = validateFileName(name);
        Path configPath = CONFIG_DIR.resolve(configName + ".vent");
        return Files.deleteIfExists(configPath);
    }


    private void handleList() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                sendError("No config has been saved!");
                return;
            }

            String configs = Files.list(CONFIG_DIR)
                    .filter(p -> p.toString().endsWith(".vent"))
                    .map(p -> p.getFileName().toString().replace(".vent", ""))
                    .collect(Collectors.joining("\n" + Formatting.GRAY + "- " + Interface.getTheme().getChatPrefixColor()));

            GameAccessor.sendChatMessage(Formatting.GRAY + "Saved configs:\n" + Formatting.GRAY + "- " + Interface.getTheme().getChatPrefixColor() + configs);
        } catch (IOException e) {
            sendError("Error printing configs!");
        }
    }

    public static void handleDelete(String[] args, boolean silent) {
        if (args.length < 2) {
            if(!silent)
                sendError("Input config name!");
            return;
        }

        try {
            String configName = validateFileName(args[1]);
            Path configPath = CONFIG_DIR.resolve(configName + ".vent");

            if (!Files.deleteIfExists(configPath)) {
                if(!silent)
                    sendError("Config " + Formatting.YELLOW + configName + Formatting.RED + " doesn't exist!");
                return;
            }

            GameAccessor.sendChatMessage(Formatting.GREEN + "Config " + Formatting.YELLOW + configName + Formatting.GREEN + " has been deleted!");
        } catch (Exception e) {
            if(!silent) sendError("Error: " + e.getMessage()); else e.printStackTrace();
        }
    }

    private static String validateFileName(String name) {
        if (name.contains("/") || name.contains("\\")) {
            throw new IllegalArgumentException("Invalid name!");
        }
        return name;
    }

    private static void sendError(String message) {
        GameAccessor.sendChatMessage(Formatting.RED + message);
    }

    private void sendUsage() {
        GameAccessor.sendChatMessage(Formatting.GRAY + "Available Commands:" + "\n" +
                Formatting.GRAY + "- " + Formatting.WHITE + ".config save " + Interface.getTheme().getChatPrefixColor() + "<name>" + "\n" +
                Formatting.GRAY + "- " + Formatting.WHITE + ".config load " + Interface.getTheme().getChatPrefixColor() + "<name>" + "\n" +
                Formatting.GRAY + "- " + Formatting.WHITE + ".config list" + "\n" +
                Formatting.GRAY + "- " + Formatting.WHITE + ".config delete " + Interface.getTheme().getChatPrefixColor() + "<name>");
    }
}
