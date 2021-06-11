package app.hibrid.hibridplayer

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import app.hibrid.hibridplayer.Api.Controller
import app.hibrid.hibridplayer.Player.MyPlayer
import app.hibrid.hibridplayer.Player.VideoPlayer
import app.hibrid.hibridplayer.Utils.*
import app.hibrid.hibridplayer.Wrapper.DaiWrapper
import app.hibrid.hibridplayer.Wrapper.ImaWrapper
import app.hibrid.hibridplayer.model.PlayerSettings
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.BehindLiveWindowException
import com.google.android.exoplayer2.source.MediaSource
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
import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.*
import java.util.concurrent.*


class HibridPlayer(
    context: Context,

    hibridSettings: HibridPlayerSettings,
    includeLayout: View,
    application: HibridApplication

) : Player.EventListener, Callback<PlayerSettings?> {

    override fun onResponse(
        call: Call<PlayerSettings?>,
        response: retrofit2.Response<PlayerSettings?>
    ) {
        if (response.isSuccessful) {
            val settings= response.body()!!
            parseSettings(settings);
        } else {
            Log.e("Request ", call.request().toString());
            Log.e("message response", response.message())
        }
    }

    private fun parseSettings(settings: PlayerSettings) {
        mGaTracker = mApplication.getDefaultTracker(settings.signature.gaTrackingId)!!;
        mUrlStreaming = settings.signature.streamUrl;
        mImaUrl = settings.signature.imaAdTag;
        mWithIma = settings.imaEnabled;
        mWithDai = settings.daiEnabled;
        mdaiApiKey = settings.signature.daiApiKey
        mDaiAssetKey = settings.signature.daiAssetKey
        mHibridSettings.baseUrl =settings.signature.streamUrl
        mAutoplay = mHibridSettings.autoplay
        initialize(reintialize = false)
    }

    override fun onFailure(call: Call<PlayerSettings?>, t: Throwable) {
        t.printStackTrace()
    }
    companion object {
        var executorService: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
        fun pause() {
            mPlayerView.keepScreenOn = false
            if (mPlayerView.player != null) {
                executorService.shutdown();
                mPlayerView.player!!.playWhenReady = false
                SendGaTrackerEvent(mGaTracker, mHibridSettings.channelKey, "pause", "pause")
            }
        }

        fun play() {
            initTimmer()
            if (mPlayerView.player != null) {
                mPlayerView.keepScreenOn = true
                mPlayerView.player!!.playWhenReady = true
                SendGaTrackerEvent(mGaTracker, mHibridSettings.channelKey, "play", "play")
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
                        SendGaTrackerEvent(mGaTracker, mHibridSettings.channelKey, "ping", "ping")
                        return
                    }
                    lastExecution = executor.submit(actualTask)
                }
            }, 15, 15, TimeUnit.SECONDS)
        }

        fun destroy() {
            executorService.shutdownNow();
            if (mPlayerView.player != null) {
                SendGaTrackerEvent(mGaTracker, mHibridSettings.channelKey, "ended", "ended")
                mPlayerView.player!!.release()
                mPlayerView.player = null;
            }
        }

        fun onConfigurationChanged(newConfig: Configuration) {
            when (newConfig.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> {
                    SendGaTrackerEvent(
                        mGaTracker,
                        mHibridSettings.channelKey,
                        "fullscreenchange",
                        "fullscreen-open"
                    )
                }
                Configuration.ORIENTATION_LANDSCAPE -> {
                    SendGaTrackerEvent(
                        mGaTracker,
                        mHibridSettings.channelKey,
                        "fullscreenchange",
                        "fullscreen-open"
                    )
                }

            }
        }

        fun getVolume(): Int {
            val audioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            var max: Double =
                audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toDouble();
            var current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toDouble();
            var percentage = (current / max * 100).toInt()

            SendGaTrackerEvent(
                mGaTracker,
                mHibridSettings.channelKey,
                "VolumeChanged",
                "$percentage"
            )
            return percentage;
        }

        lateinit var mHibridSettings: HibridPlayerSettings;
        lateinit var mUrlStreaming: String;
        lateinit var mPlayerView: PlayerView;
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
        lateinit var mIncludeLayout: View;
        lateinit var mApplication: HibridApplication;
    }
    init {
        mApplication = application;
        mIncludeLayout = includeLayout;
        mHibridSettings = hibridSettings;
        mPlayerView = mIncludeLayout.findViewById(R.id.hibridPlayerView);
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

    fun initialize(reintialize: Boolean) {
        val videoTrackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        player = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector)
        player.addListener(this)
        if (mWithDai) {
            sampleVideoPlayer = VideoPlayer(
                mContext,
                mPlayerView,
                mWithIma,
                mImaUrl,
                mGaTracker, mHibridSettings
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
                mHibridSettings
            )
            if (mWithDai)
                sampleAdsWrapper.requestAndPlayAds()
        } else {
            val defaultBandwidthMeter = DefaultBandwidthMeter.Builder(mContext).build()
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
                        hibridSettings = mHibridSettings
                    )
                } else
                    HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
            MyPlayer().init(
                player = player,
                mediaSource = mMediaSource,
                playerView = mPlayerView,
                autoplay = mAutoplay,
                gaTracker = mGaTracker,
                hibridSettings = mHibridSettings

            )
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        if (isBehindLiveWindow(error)) {
            initialize(reintialize = true)
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


