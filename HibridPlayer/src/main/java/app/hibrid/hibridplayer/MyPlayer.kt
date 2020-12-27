package app.hibrid.hibridplayer

import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.metadata.id3.TextInformationFrame
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.ui.PlayerView

class MyPlayer() {

    companion object {
        lateinit var mPlayer: SimpleExoPlayer;
        lateinit var mMediaSource: MediaSource;
        lateinit var mPlayerView: PlayerView;
        var mAutoplay:Boolean=false;
        private var playerCallback: SampleVideoPlayerCallback? = null
    }
    interface SampleVideoPlayerCallback {
        fun onUserTextReceived(userText: String?)
        fun onSeek(windowIndex: Int, positionMs: Long)
    }

    fun init(
        player: SimpleExoPlayer,
        mediaSource: MediaSource,
        playerView: PlayerView,
        autoplay: Boolean
    ) {

        mAutoplay = autoplay;
        mPlayer = player;
        mMediaSource = mediaSource;
        mPlayerView = playerView;
        mPlayer.addMediaSource(mMediaSource);
        mPlayerView.player = mPlayer
        mPlayer.playWhenReady = mAutoplay
        mPlayer.prepare()
        mPlayerView.keepScreenOn = true
        mPlayerView.controllerShowTimeoutMs = 1000

        mPlayerView.setControlDispatcher(
            object : ControlDispatcher {
                override fun dispatchSetPlayWhenReady(
                    player: Player,
                    playWhenReady: Boolean
                ): Boolean {
                    player.playWhenReady = playWhenReady
                    return playWhenReady
                }
                override fun isRewindEnabled(): Boolean {
                    return false
                }
                override fun isFastForwardEnabled(): Boolean {
                    return false
                }
                override fun dispatchFastForward(p: Player): Boolean {
                    return false
                }
                override fun dispatchRewind(p: Player): Boolean {
                    return false
                }
                override fun dispatchNext(p: Player): Boolean {
                    return false
                }
                override fun dispatchPrevious(p: Player): Boolean {
                    return false
                }
                override fun dispatchSeekTo(
                    player: Player,
                    windowIndex: Int,
                    positionMs: Long
                ): Boolean {
//                    player.seekTo(windowIndex, positionMs)
                    return false
                }
                override fun dispatchSetRepeatMode(player: Player, repeatMode: Int): Boolean {
                    return false
                }
                override fun dispatchSetShuffleModeEnabled(
                    player: Player,
                    shuffleModeEnabled: Boolean
                ): Boolean {
                    return false
                }
                override fun dispatchStop(player: Player, reset: Boolean): Boolean {
                    return false
                }
            })
        // Register for ID3 events.

    }

    fun setSampleVideoPlayerCallback(callback: SampleVideoPlayerCallback) {
        playerCallback = callback
    }

    fun enableControls(doEnable: Boolean) {
        if (doEnable) {
            mPlayerView.showController()
        } else {
            mPlayerView.hideController()
        }
    }
}