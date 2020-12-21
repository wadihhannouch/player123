package app.hibrid.hibridplayer

import android.os.Handler
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceEventListener
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.video.VideoListener

class MyPlayer : Player.EventListener, VideoListener, MediaSourceEventListener {
    fun init(
        player: SimpleExoPlayer,
        myMediaSource: MediaSource,
        playerView: PlayerView,
        smoothStreaminHibridPlayer: SmoothStreaminHibridPlayer
    ){
        player.addListener(smoothStreaminHibridPlayer)
        player.addVideoListener(smoothStreaminHibridPlayer)
        player.addMediaSource(myMediaSource)

        myMediaSource.addEventListener(Handler(), smoothStreaminHibridPlayer)
        playerView.player = player
        SmoothStreaminHibridPlayer.mImaAdsLoader.setPlayer(player);
        player.playWhenReady = true
        player.prepare()
    }

}