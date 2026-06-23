package lol.ventura.features.command;

import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.command.Command;
import lol.ventura.foundation.module.Module;
import lol.ventura.foundation.module.ModuleRepository;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.Arrays;

public class BindCommand extends Command {
    public BindCommand() {
        super("bind",
                "bind",
                "bind <module> [key]",
                "bind remove <module>"
        );
    }

    @Override
    public void execute(String... args) {
        if (args.length == 0) {
            listBinds();
            return;
        }

        if (args[0].equalsIgnoreCase("remove")) {
            handleRemoveCommand(args);
            return;
        }

        handleBindCommand(args);
    }

    private void handleBindCommand(String[] args) {
        String[] nameParts = Arrays.copyOfRange(args, 0, args.length - 1);
        String moduleName = String.join(" ", nameParts);
        String keyInput = args[args.length - 1];

        Module module = findModule(moduleName);
        if (module == null) {
            GameAccessor.sendChatMessage("§cModule not found: " + moduleName);
            return;
        }

        try {
            int key = parseKeyInput(keyInput);
            updateBind(module, key);
        } catch (IllegalArgumentException e) {
            GameAccessor.sendChatMessage("§cInvalid key: " + keyInput);
        }
    }

    private void handleRemoveCommand(String[] args) {
        if (args.length < 2) {
            GameAccessor.sendChatMessage("§cUsage: .bind remove <module>");
            return;
        }

        String[] nameParts = Arrays.copyOfRange(args, 1, args.length);
        String moduleName = String.join(" ", nameParts);
        Module module = findModule(moduleName);

        if (module == null) {
            GameAccessor.sendChatMessage("§cModule not found: " + moduleName);
            return;
        }

        resetToDefault(module);
    }

    private void resetToDefault(Module module) {
        int defaultKey = module.getDescriptor().key();
        module.setKey(defaultKey);
        GameAccessor.sendChatMessage(String.format(
                "§aReset bind for §e%s §ato default (§e%s§a)",
                module.getDescriptor().name(),
                getKeyName(defaultKey)
        ));
    }

    private Module findModule(String name) {
        return ModuleRepository.getInstance().getModules().stream()
                .filter(m -> m.getDescriptor().name().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElse(null);
    }

    private int parseKeyInput(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return getKeyCode(input.toUpperCase());
        }
    }

    private int getKeyCode(String keyName) {
        try {
            Field field = GLFW.class.getField("GLFW_KEY_" + keyName);
            return field.getInt(null);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown key: " + keyName);
        }
    }

    private String getKeyName(int keyCode) {
        try {
            for (Field field : GLFW.class.getFields()) {
                if (field.getName().startsWith("GLFW_KEY_") && field.getInt(null) == keyCode) {
                    return field.getName().substring(9);
                }
            }
        } catch (Exception ignored) {}
        return "Unknown";
    }

    private void listBinds() {
        StringBuilder sb = new StringBuilder("§6Active binds:\n");
        ModuleRepository.getInstance().getModules().forEach(module ->
                sb.append("§7- §e")
                        .append(module.getDescriptor().name())
                        .append(" §7(§f")
                        .append(getKeyName(module.getKey()))
                        .append("§7)\n")
        );
        GameAccessor.sendChatMessage(sb.toString());
    }

    private void updateBind(Module module, int key) {
        module.setKey(key);
        GameAccessor.sendChatMessage(String.format(
                "§aModule §e%s §abound to §e%s",
                module.getDescriptor().name(),
                getKeyName(key)
        ));
    }
}