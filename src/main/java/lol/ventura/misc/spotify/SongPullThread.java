package lol.ventura.misc.spotify;

import lombok.Getter;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.specification.Track;

public class SongPullThread implements Runnable{

    @Getter
    private IPlaylistItem track;

    @Getter
    private Track trackDetails;

    @Getter
    private int progress;

    private String prevSong = "";

    @Override
    public void run() {
        while(true)
        {
            try {
                if(!SpotifyService.isInitialized())
                {
                    Thread.sleep(5000);
                    continue;
                }

                CurrentlyPlaying playing = SpotifyService.getApi().getUsersCurrentlyPlayingTrack().build().execute();
                if(playing == null)
                {
                    Thread.sleep(5000);
                    continue;
                }

                this.track = playing.getItem();
                this.progress = playing.getProgress_ms();


                if(!this.track.getId().equalsIgnoreCase(prevSong))
                {
                    this.trackDetails = SpotifyService.getApi().getTrack(playing.getItem().getId()).build().execute();
                }

                prevSong = this.track.getId();
                Thread.sleep(1000);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
