package lol.ventura.features.command;

import lol.ventura.features.ui.Spotify;
import lol.ventura.foundation.GameAccessor;
import lol.ventura.foundation.command.Command;
import lol.ventura.misc.spotify.SpotifyService;

public class SpotifyCommand extends Command {
    public SpotifyCommand() {
        super("spotify", "spotify");
    }

    @Override
    public void execute(String... args) {
        try{
            SpotifyService.getApi().skipUsersPlaybackToNextTrack().build().execute();
        }catch (Exception e)
        {
            GameAccessor.sendChatMessage(e.getMessage());
        }
    }
}
