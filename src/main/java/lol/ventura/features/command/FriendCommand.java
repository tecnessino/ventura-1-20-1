package lol.ventura.features.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.command.Command;
import lol.ventura.misc.storage.StorageService;

import java.util.ArrayList;
import java.util.List;

public class FriendCommand extends Command {

    public FriendCommand() {
        super("friend",
                "friend",
                "Manage friends",
                "friend add <name>",
                "friend remove <name>",
                "friend list"
        );
    }

    @Override
    public void execute(String... args) {
        if (args.length == 0) {
            sendUsage();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "add":
                handleAdd(args);
                break;
            case "remove":
                handleRemove(args);
                break;
            case "list":
                handleList();
                break;
            default:
                sendError("Invalid operation: " + args[0]);
        }
    }

    private void handleAdd(String[] args) {
        if (args.length < 2) {
            sendError("Please specify a player name!");
            return;
        }

        String playerName = args[1];
        if (!isValidPlayerName(playerName)) {
            sendError("Invalid player name!");
            return;
        }

        List<String> friends = getFriends();
        if (friends.contains(playerName)) {
            sendError("Player §e" + playerName + " §cis already a friend!");
            return;
        }

        friends.add(playerName);
        saveFriends(friends);
        GameAccessor.sendChatMessage("§aAdded §e" + playerName + " §ato friends!");
    }

    private void handleRemove(String[] args) {
        if (args.length < 2) {
            sendError("Please specify a player name!");
            return;
        }

        String playerName = args[1];
        List<String> friends = getFriends();
        if (!friends.contains(playerName)) {
            sendError("Player §e" + playerName + " §cis not a friend!");
            return;
        }

        friends.remove(playerName);
        saveFriends(friends);
        GameAccessor.sendChatMessage("§aRemoved §e" + playerName + " §afrom friends!");
    }

    private void handleList() {
        List<String> friends = getFriends();
        if (friends.isEmpty()) {
            GameAccessor.sendChatMessage("§cYou have no friends added!");
            return;
        }

        String friendList = String.join("\n§7- §e", friends);
        GameAccessor.sendChatMessage("§6Friends:\n§7- §e" + friendList);
    }

    private List<String> getFriends() {
        List<String> friends = new ArrayList<>();
        JsonObject friendData = StorageService.getInstance().getForClass(FriendCommand.class);
        JsonArray friendArray = friendData.getAsJsonArray("friends");
        if (friendArray != null) {
            for (JsonElement element : friendArray) {
                friends.add(element.getAsString());
            }
        }
        return friends;
    }

    private void saveFriends(List<String> friends) {
        JsonObject friendData = StorageService.getInstance().getForClass(FriendCommand.class);
        JsonArray friendArray = new JsonArray();
        for (String friend : friends) {
            friendArray.add(friend);
        }
        friendData.add("friends", friendArray);
    }

    private boolean isValidPlayerName(String name) {
        return name != null && name.matches("[a-zA-Z0-9_]{3,16}");
    }

    private void sendError(String message) {
        GameAccessor.sendChatMessage("§c" + message);
    }

    private void sendUsage() {
        GameAccessor.sendChatMessage("§6Available Commands:");
        GameAccessor.sendChatMessage("§7- §e.friend add <name>");
        GameAccessor.sendChatMessage("§7- §e.friend remove <name>");
        GameAccessor.sendChatMessage("§7- §e.friend list");
    }
}