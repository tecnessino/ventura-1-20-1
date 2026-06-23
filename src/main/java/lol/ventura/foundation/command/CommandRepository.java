package lol.ventura.foundation.command;

import lol.ventura.VenturaClient;
import lol.ventura.features.command.*;
import lol.ventura.foundation.Service;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public final class CommandRepository extends Service {

    @Getter
    @Setter
    private static CommandRepository instance;
    @Getter
    private final ArrayList<Command> commands = new ArrayList<>();
    @Getter
    private final String prefix = ".";

    public CommandRepository() {
        commands.addAll(List.of(new HelpCommand(), new BindCommand(), new StorageCommand(), new ConfigCommand(), new SpotifyCommand(), new FriendCommand()));
    }
}
