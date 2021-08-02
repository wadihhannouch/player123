package app.hibrid.hibridplayer.Player

import app.hibrid.hibridplayer.Utils.HibridPlayerSettings
import app.hibrid.hibridplayer.Utils.SendGaTrackerEvent
import app.hibrid.hibridplayer.Wrapper.ImaWrapper
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.video.VideoListener
import com.google.android.gms.analytics.Tracker


class MyPlayer() : Player.EventListener, VideoListener {

    companion object {
        lateinit var mPlayer: SimpleExoPlayer;
        lateinit var mMediaSource: MediaSource;
        lateinit var mPlayerView: PlayerView;
        lateinit var mGaTracker: Tracker;
        lateinit var mChannelId: String;
        lateinit var mHibridSettings: HibridPlayerSettings
        var mAutoplay:Boolean=false;
    }

    override fun onTracksChanged(
        trackGroups: TrackGroupArray,
        trackSelections: TrackSelectionArray
    ) {
        if(trackGroups.isEmpty){

        }
//        super.onTracksChanged(trackGroups, trackSelections)
    }

    fun init(
        player: SimpleExoPlayer,
        mediaSource: MediaSource,
        playerView: PlayerView,
        autoplay: Boolean,
        gaTracker: Tracker,
        hibridSettings: HibridPlayerSettings,
        channelId: String?
    ) {

        mGaTracker = gaTracker
        mHibridSettings = hibridSettings
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
        mChannelId = channelId!!
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
        mPlayer.addListener(this)
        mPlayer.addVideoListener(this)
        var x = mPlayer.mediaItemCount
        for( i in 0..x){
            var media = mPlayer.getMediaItemAt(i)
            var current = mPlayer.currentTrackSelections
            var track = mPlayer.trackSelector
            var trackGroup = mPlayer.currentTrackGroups

        }
    }

    override fun onVideoSizeChanged(
        width: Int,
        height: Int,
        unappliedRotationDegrees: Int,
        pixelWidthHeightRatio: Float
    ) {
        SendGaTrackerEvent(
            mGaTracker,
            mChannelId,
            "Video Falvor",
            "${width}p"
        )
        super.onVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        var playingTitle = if(isPlaying ) "Play" else "Pause";
        SendGaTrackerEvent(
            mGaTracker,
            channelKey = mChannelId,
            title = playingTitle,
            description = playingTitle
        )

        super.onIsPlayingChanged(isPlaying)
    }
}