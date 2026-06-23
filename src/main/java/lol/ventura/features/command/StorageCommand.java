package lol.ventura.features.command;

import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.command.Command;
import lol.ventura.misc.storage.StorageService;

public class StorageCommand extends Command {
    public StorageCommand() {
        super("storage", "save");
    }

    @Override
    public void execute(String... args) {
        StorageService.getInstance().save();
        GameAccessor.sendChatMessage("saved");
    }
}