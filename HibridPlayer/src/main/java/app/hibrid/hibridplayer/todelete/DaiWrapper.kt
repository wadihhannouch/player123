package app.hibrid.hibridplayer.todelete


import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import app.hibrid.hibridplayer.MyPlayer
import com.google.ads.interactivemedia.v3.api.*
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate
import com.google.ads.interactivemedia.v3.api.player.VideoStreamPlayer
import com.google.ads.interactivemedia.v3.api.player.VideoStreamPlayer.VideoStreamPlayerCallback
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import java.util.*


class DaiWrapper(
    player: SimpleExoPlayer,
    playerView: PlayerView,
    context: Context,
    adUicontainer: ViewGroup,
    daiAssetKey: String,
    daiApiKey: String?,
    withIma: Boolean,
    imaUrl: String,
    autoplay: Boolean,
    reintialize: Boolean,
    requested: Boolean,
    streamUrl: String

) : AdsLoader.AdsLoadedListener, AdErrorEvent.AdErrorListener, AdEvent.AdEventListener {
    companion object {
        lateinit var mPlayer: SimpleExoPlayer;
        lateinit var mPlayerView: PlayerView;
        lateinit var mContext: Context;
        lateinit var mAdUicontainer: ViewGroup;
        lateinit var adsLoader: AdsLoader;
        lateinit var mDaiAssetKey: String;
        var mDaiApiKey: String? = null;
        lateinit var mImaUrl: String;
        lateinit var mStreamUrl: String;
        lateinit var streamManager: StreamManager;
        var mWithIma: Boolean = false;
        var mRequested: Boolean = false;
        var mAutoplay: Boolean = false;
        var mReintialize: Boolean = false;
        lateinit var playerCallbacks: MutableList<VideoStreamPlayerCallback>;
        val myPlayer = MyPlayer();
        lateinit var mVideoAdPlayer:VideoAdPlayer;
        lateinit var displayContainer: StreamDisplayContainer;
        private const val PLAYER_TYPE = "DAISamplePlayer"
    }

    init {
        mStreamUrl = streamUrl
        mRequested = requested
        mReintialize = reintialize
        mAutoplay = autoplay
        mPlayer = player
        mPlayerView = playerView
        mContext = context;
        mAdUicontainer = adUicontainer;
        mWithIma = withIma
        mImaUrl = imaUrl
        mDaiApiKey = daiApiKey
        mDaiAssetKey = daiAssetKey
        init()
    }

    fun init() {


        val sdkFactory = ImaSdkFactory.getInstance()
        playerCallbacks = mutableListOf<VideoStreamPlayerCallback>();
        createAdsLoader(sdkFactory)
//        val videoStreamPlayer: VideoStreamPlayer = createVideoStreamPlayer();
//
//
//
//        val displayContainer = ImaSdkFactory.createStreamDisplayContainer(
//            mAdUicontainer,
//            videoStreamPlayer
//        )
//
//        val settings = sdkFactory.createImaSdkSettings()
//
//        adsLoader = sdkFactory.createAdsLoader(mContext, settings, displayContainer)
//
//        myPlayer.setSampleVideoPlayerCallback(
//            object : MyPlayer.SampleVideoPlayerCallback {
//                override fun onUserTextReceived(userText: String?) {
//                    for (callback in playerCallbacks!!) {
//                        callback.onUserTextReceived(userText)
//                    }
//                }
//
//                override fun onSeek(windowIndex: Int, positionMs: Long) {
//                }
//            })
//
//
//        requestPlayAds(sdkFactory)
    }

    private fun createAdsLoader(sdkFactory: ImaSdkFactory) {
        val settings = sdkFactory.createImaSdkSettings()
        settings.playerType = PLAYER_TYPE

        val videoStreamPlayer = createVideoStreamPlayer()
        displayContainer =
            ImaSdkFactory.createStreamDisplayContainer(mAdUicontainer, videoStreamPlayer)
        myPlayer.setSampleVideoPlayerCallback(
            object : MyPlayer.SampleVideoPlayerCallback {
                override fun onUserTextReceived(userText: String?) {
                    for (callback in playerCallbacks) {
                        callback.onUserTextReceived(userText)
                    }
                }
                override fun onSeek(windowIndex: Int, positionMs: Long) {
                }
            })
        adsLoader = sdkFactory.createAdsLoader(mContext, settings, displayContainer)
    }

    private fun requestPlayAds(sdkFactory: ImaSdkFactory) {
        val request: StreamRequest = sdkFactory.createLiveStreamRequest(
            mDaiAssetKey,
            mDaiApiKey
        );
        adsLoader.addAdErrorListener(this)
        adsLoader.addAdsLoadedListener(this)
        adsLoader.requestStream(request)
    }


    private fun createVideoStreamPlayer(): VideoStreamPlayer {
        return object : VideoStreamPlayer {
            override fun getContentProgress(): VideoProgressUpdate {
                return VideoProgressUpdate(
                    mPlayer.bufferedPosition, mPlayer.duration
                );
            }
            override fun getVolume(): Int {
                return 100
            }
            override fun loadUrl(url: String?, subtitles: List<HashMap<String?, String?>?>?) {
                Log.d("Dai Url", url!!)
                createMediaSources(url)
            }
            override fun pause() {
                mPlayer.pause()
            }
            override fun resume() {
                mPlayer.play()
            }
            override fun addCallback(videoStreamPlayerCallback: VideoStreamPlayerCallback) {
                playerCallbacks.add(videoStreamPlayerCallback)
                Log.d("wadih", "addCallback")
            }
            override fun removeCallback(videoStreamPlayerCallback: VideoStreamPlayerCallback) {
                playerCallbacks.remove(videoStreamPlayerCallback)
                Log.d("wadih", "removeCallback ")
            }
            override fun onAdBreakStarted() {
                myPlayer.enableControls(false);
                Log.d("wadih onAdBreakStarted", "onAdBreakStarted")
            }

            override fun onAdBreakEnded() {
                myPlayer.enableControls(true);
                Log.d("wadih onAdBreakEnded", "onAdBreakEnded")
//                mPlayerView.hideController()
            }
            override fun onAdPeriodStarted() {

                Log.d("wadih", "onAdPeriodStarted")
            }

            override fun onAdPeriodEnded() {
                Log.d("wadih", "onAdPeriodEnded")
            }

            override fun seek(p0: Long) {
                mPlayer.seekTo(p0)
            }
        }
    }

    fun createMediaSources(url: String) {
        if (!mRequested) {
            val defaultBandwidthMeter = DefaultBandwidthMeter.Builder(mContext).build()
            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                mContext,
                Util.getUserAgent(mContext, "Exo2"), defaultBandwidthMeter
            )
            val mediaItem = MediaItem.Builder().setUri(Uri.parse(url)).build()
            val mediaSource =
//                if (mWithIma && !mReintialize) {
//                    ImaWrapper().init(
//                        url = url,
//                        playerView = mPlayerView,
//                        player = mPlayer,
//                        imaUrl = mImaUrl,
//                        context = mContext,
//                        gaTracker = mGaTracker
//                    )
//                } else {
                    HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
//                }
            val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
            mediaSourceFactory.setAdViewProvider(mPlayerView)
            MyPlayer().init(
                player = mPlayer,
                mediaSource = mediaSource,
                playerView = mPlayerView,
                autoplay = mAutoplay
            )
            mRequested = true
        }
    }

    override fun onAdsManagerLoaded(p0: AdsManagerLoadedEvent?) {
        streamManager = p0!!.streamManager;
        streamManager.addAdErrorListener(this);
        streamManager.addAdEventListener(this);
        streamManager.init();
        Log.d("AdsManagerLoadedEvent", p0.toString())
    }

    override fun onAdError(p0: AdErrorEvent?) {
        Log.e("AdErrorEvent", p0!!.error.message.toString())

    }

    override fun onAdEvent(event: AdEvent?) {
        when (event!!.getType()) {
            AdEventType.AD_PROGRESS -> {
            }
            else -> Log.d("", String.format("Event: %s\n", event.getType()))
        }
    }
}