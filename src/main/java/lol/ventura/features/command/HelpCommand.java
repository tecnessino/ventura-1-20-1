package lol.ventura.features.command;

import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.command.Command;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "help","guwno");
    }

    @Override
    public void execute(String... args) {
        try {
            GameAccessor.sendChatMessage("full work");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}