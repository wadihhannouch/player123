package app.hibrid.hibridplayer

import android.content.Context
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import app.hibrid.hibridplayer.Api.Controller
import app.hibrid.hibridplayer.Player.MyPlayer
import app.hibrid.hibridplayer.Player.VideoPlayer
import app.hibrid.hibridplayer.Utils.HashUtils
import app.hibrid.hibridplayer.Utils.HibridApplication
import app.hibrid.hibridplayer.Utils.HibridPlayerSettings
import app.hibrid.hibridplayer.Utils.SendGaTrackerEvent
import app.hibrid.hibridplayer.Wrapper.DaiWrapper
import app.hibrid.hibridplayer.Wrapper.ImaWrapper
import app.hibrid.hibridplayer.model.PlayerSettings
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.*
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
import com.google.android.gms.analytics.Tracker
import retrofit2.Call
import retrofit2.Callback
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class HibridPlayer(
    context: Context,

    hibridSettings: HibridPlayerSettings,
    includeLayout: View,
    application: HibridApplication

) : Player.EventListener, Callback<PlayerSettings?>, MediaSourceEventListener {

    override fun onDownstreamFormatChanged(
        windowIndex: Int,
        mediaPeriodId: MediaSource.MediaPeriodId?,
        mediaLoadData: MediaLoadData
    ) {
        super.onDownstreamFormatChanged(windowIndex, mediaPeriodId, mediaLoadData)
    }

    override fun onUpstreamDiscarded(
        windowIndex: Int,
        mediaPeriodId: MediaSource.MediaPeriodId,
        mediaLoadData: MediaLoadData
    ) {
        super.onUpstreamDiscarded(windowIndex, mediaPeriodId, mediaLoadData)
    }

    init {
        mApplication = application;
        mIncludeLayout = includeLayout;
        mHibridSettings = hibridSettings;
        mPlayerView = mIncludeLayout.findViewById(R.id.hibridPlayerView);
        pb = mIncludeLayout.findViewById(R.id.pb);
        mAdUicontainer = mIncludeLayout.findViewById(R.id.adUiContainer);
        mContext = context;

        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER)
        if (CookieHandler.getDefault() !== cookieManager) {
            CookieHandler.setDefault(cookieManager)
        }

        var channelKey = mHibridSettings.channelKey
        val timestamp = (System.currentTimeMillis() / 1000).toInt();
        Log.d("timestamp", timestamp.toString());
        val lisenceKey = mHibridSettings.lisence
        val myHexHash: String = HashUtils.getSHA1(timestamp.toString() + lisenceKey);
        var controller = Controller()
        var x = controller.start(channelKey, timestamp.toString(), myHexHash);
        x.enqueue(this);

    }
    override fun onResponse(
        call: Call<PlayerSettings?>,
        response: retrofit2.Response<PlayerSettings?>
    ) {
        if (response.isSuccessful) {
            val settings= response.body()!!
            parseSettings(settings);
        } else {
            Log.e("Request ", call.request().toString())
            Log.e("message response", response.message())
        }
    }

    private fun parseSettings(settings: PlayerSettings) {
        mGaTracker = mApplication.getDefaultTracker(settings.signature.gaTrackingId)!!
        mUrlStreaming = settings.signature.streamUrl
        mImaUrl = settings.signature.imaAdTag
        mWithIma = settings.imaEnabled
        mWithDai = settings.daiEnabled
        mdaiApiKey = settings.signature.daiApiKey
        mDaiAssetKey = settings.signature.daiAssetKey
        mHibridSettings.baseUrl =settings.signature.streamUrl
        mAutoplay = mHibridSettings.autoplay
        mChannelId = settings.channelId
        initialize(reintialize = false)
    }

    override fun onFailure(call: Call<PlayerSettings?>, t: Throwable) {
        t.printStackTrace()
    }

    companion object {
        lateinit var mHibridSettings: HibridPlayerSettings;
        lateinit var mUrlStreaming: String;
        lateinit var mPlayerView: PlayerView;
        lateinit var pb: ProgressBar;
        lateinit var mContext: Context;
        lateinit var mImaUrl: String;
        lateinit var mAdUicontainer: ViewGroup;
        lateinit var mDaiAssetKey: String;
        var mdaiApiKey: String? = null;
        lateinit var mMediaSource: MediaSource;
        var mWithIma: Boolean = false;
        var mWithDai: Boolean = false;
        var mAutoplay: Boolean = false;
        lateinit var sampleVideoPlayer: VideoPlayer;
        lateinit var player: SimpleExoPlayer;
        lateinit var mGaTracker: Tracker;
        var mChannelId: String? = "";
        lateinit var mIncludeLayout: View;
        lateinit var mApplication: HibridApplication;
        var executorService: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
        fun pause() {
            mPlayerView.keepScreenOn = false
            if (mPlayerView.player != null) {
                executorService.shutdown();
                mPlayerView.player!!.playWhenReady = false
                SendGaTrackerEvent(mGaTracker, mChannelId!!, "pause", "pause")
            }
        }

        fun play() {
            initTimmer()
            if (mPlayerView.player != null) {
                mPlayerView.keepScreenOn = true
                mPlayerView.player!!.playWhenReady = true
                SendGaTrackerEvent(mGaTracker, mChannelId!!, "play", "play")
            }
        }

        fun initTimmer() {
            if (executorService.isShutdown)
                executorService = Executors.newScheduledThreadPool(1)
            val actualTask: Runnable? = null
            executorService.scheduleAtFixedRate(object : Runnable {
                private val executor = Executors.newSingleThreadExecutor()
                private var lastExecution: Future<*>? = null
                override fun run() {
                    if (lastExecution != null && !lastExecution!!.isDone()) {
                        SendGaTrackerEvent(mGaTracker, mChannelId!!, "ping", "ping")
                        return
                    }
                    lastExecution = executor.submit(actualTask)
                }
            }, 15, 15, TimeUnit.SECONDS)
        }

        fun destroy() {
            executorService.shutdownNow();
            if (mPlayerView.player != null) {
                SendGaTrackerEvent(mGaTracker, mChannelId!!, "ended", "ended")
                mPlayerView.player!!.release()
                mPlayerView.player = null;
            }
        }

        fun onConfigurationChanged(newConfig: Configuration) {
            when (newConfig.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> {
                    SendGaTrackerEvent(
                        mGaTracker,
                        mChannelId!!,
                        "fullscreenchange",
                        "fullscreen-open"
                    )
                }
                Configuration.ORIENTATION_LANDSCAPE -> {
                    SendGaTrackerEvent(
                        mGaTracker,
                        mChannelId!!,
                        "fullscreenchange",
                        "fullscreen-open"
                    )
                }

            }
        }

        fun getVolume(): Int {
            val audioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            val max: Double =
                audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toDouble();
            val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toDouble();
            val percentage = (current / max * 100).toInt()

            SendGaTrackerEvent(
                mGaTracker,
                mChannelId!!,
                "VolumeChanged",
                "$percentage"
            )
            return percentage;
        }


    }

    fun initialize(reintialize: Boolean) {
        val defaultBandwidthMeter = DefaultBandwidthMeter.Builder(mContext).build()
        val videoTrackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory()
        val trackSelector =  DefaultTrackSelector(videoTrackSelectionFactory)
//        player = ExoPlayer.Builder(mContext,trackSelector, mMediaSource)
        player = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector)
        player.addListener(this)
        if (mWithDai) {
            sampleVideoPlayer = VideoPlayer(
                mContext,
                mPlayerView,
                mWithIma,
                mImaUrl,
                mGaTracker, mHibridSettings,
                defaultBandwidthMeter = defaultBandwidthMeter,
                channelId = mChannelId

            )
            sampleVideoPlayer.enableControls(false)
            val sampleAdsWrapper = DaiWrapper(
                mContext,
                sampleVideoPlayer,
                mAdUicontainer,
                mWithIma,
                mImaUrl,
                mDaiAssetKey,
                mdaiApiKey,
                mGaTracker,

                mHibridSettings,
                defaultBandwidthMeter = defaultBandwidthMeter,
                channelId = mChannelId
            )
            if (mWithDai)
                sampleAdsWrapper.requestAndPlayAds()
        } else {

            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                mContext,
                Util.getUserAgent(mContext, "Exo2"), defaultBandwidthMeter
            )
            val url = if (mWithIma && !reintialize) {
                mImaUrl
            } else mUrlStreaming
            val mediaItem = MediaItem.fromUri(Uri.parse(url))
            mMediaSource =
                if (mWithIma) {
                    ImaWrapper().init(
                        url = mUrlStreaming,
                        playerView = mPlayerView,
                        player = player,
                        imaUrl = url,
                        context = mContext,
                        gaTracker = mGaTracker,
                        hibridSettings = mHibridSettings,
                        defaultBandwidthMeter = defaultBandwidthMeter,
                        channelId= mChannelId
                    )
                } else {
                   HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
                }

            mMediaSource.addEventListener(Handler(), this)
            MyPlayer().init(
                player = player,
                mediaSource = mMediaSource,
                playerView = mPlayerView,
                autoplay = mAutoplay,
                gaTracker = mGaTracker,
                hibridSettings = mHibridSettings,
                channelId= mChannelId

            )
        }
    }

    override fun onPlaybackStateChanged(state: Int) {
        when(state){
            ExoPlayer.STATE_READY -> {
                pb.visibility = View.GONE
            }
            ExoPlayer.STATE_BUFFERING -> {
                pb.visibility = View.VISIBLE
            }
            ExoPlayer.STATE_IDLE -> {
            }
            ExoPlayer.STATE_ENDED -> {
            }
        }
        if(state == ExoPlayer.STATE_READY){
            print("exo state readu")
            player.trackSelector
        }

        super.onPlaybackStateChanged(state)
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        if (isBehindLiveWindow(error)) {
            initialize(reintialize = true)
        }
        else{
            SendGaTrackerEvent(
                mGaTracker,
                mChannelId!!,
                "Debug Error",
                error.message.toString()
            )
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


