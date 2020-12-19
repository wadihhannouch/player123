package app.hibrid.hibridplayer

import android.content.Context
import android.net.Uri
import android.os.Handler
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.BehindLiveWindowException
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceEventListener
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoListener
import java.io.IOException

class SmoothStreaminHibridPlayer(urlStreaming: String, playerView: PlayerView, context: Context) : Player.EventListener, VideoListener, MediaSourceEventListener {
    companion object {

        lateinit var url:String
        lateinit var playerViewE :PlayerView;
        lateinit var con : Context;
    }

    init {
        url = urlStreaming;
        playerViewE = playerView
        con = context
        initializePlayer(urlStreaming, playerView, context)
    }



    fun initializePlayer(urlStreaming: String, playerView: PlayerView, context: Context){
        val adaptiveTrackSelection: TrackSelection.Factory = AdaptiveTrackSelection.Factory(DefaultBandwidthMeter())
        var player = ExoPlayerFactory.newSimpleInstance(
                context,
                DefaultRenderersFactory(context),
                DefaultTrackSelector(adaptiveTrackSelection))
        player.addListener(this)
        player.addVideoListener(this)

        playerView.player = player

        val defaultBandwidthMeter = DefaultBandwidthMeter()
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "Exo2"), defaultBandwidthMeter)

        val uri = Uri.parse(urlStreaming)
        val mediaSource: MediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
        mediaSource.addEventListener(Handler(), this)
        player.prepare(mediaSource)
        player.playWhenReady = true
        player.prepare(mediaSource, true, false)
    }

    //
    override fun onLoadStarted(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?, loadEventInfo: MediaSourceEventListener.LoadEventInfo?, mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
        Log.d("Hibrid Player Log", "Hibrid Player Log: LoadStarted smoothStreaminng")
    }

    override fun onDownstreamFormatChanged(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?, mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
        print("Hibrid Player Log: DownstreamFormatChanged")
    }

    override fun onUpstreamDiscarded(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?, mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
        Log.d("Hibrid Player Log", "Hibrid Player Log: UpstreamDiscarded")
    }

    override fun onMediaPeriodCreated(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
        Log.d("Hibrid Player Log", "Hibrid Player Log: MediaPeriodCreated")
    }

    override fun onLoadCanceled(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?, loadEventInfo: MediaSourceEventListener.LoadEventInfo?, mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
        Log.d("Hibrid Player Log", "Hibrid Player Log: LoadCanceled")
    }

    override fun onMediaPeriodReleased(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
        Log.d("Hibrid Player Log", "Hibrid Player Log:  MediaPeriodReleased")
    }

    override fun onReadingStarted(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {
        Log.d("Hibrid Player Log", "Hibrid Player Log:  ReadingStarted")
    }

    override fun onLoadCompleted(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?, loadEventInfo: MediaSourceEventListener.LoadEventInfo?, mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
        Log.d("Hibrid Player Log", "Hibrid Player Log:  LoadCompleted")
    }

    override fun onLoadError(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?, loadEventInfo: MediaSourceEventListener.LoadEventInfo?, mediaLoadData: MediaSourceEventListener.MediaLoadData?, error: IOException?, wasCanceled: Boolean) {
        Log.d("Hibrid Player Log", "Hibrid Player Log: LoadError")
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        Log.e("PLAYER_ACTIVITY_TAG", "SOME ERROR IN PLAYER");
        if (isBehindLiveWindow(error)) {
            initializePlayer(url, playerViewE, con);
        }
    }
    private fun isBehindLiveWindow(e: ExoPlaybackException): Boolean {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false
        }
        var cause: Throwable? = e.sourceException
        while (cause != null) {
            if (cause is BehindLiveWindowException) {
                return true
            }
            cause = cause.cause
        }
        return false
    }


}