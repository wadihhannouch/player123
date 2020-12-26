package app.hibrid.hibridplayer


import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import app.hibrid.hibridplayer.MyPlayer.SampleVideoPlayerCallback
import com.google.ads.interactivemedia.v3.api.*
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate
import com.google.ads.interactivemedia.v3.api.player.VideoStreamPlayer
import com.google.ads.interactivemedia.v3.api.player.VideoStreamPlayer.VideoStreamPlayerCallback
import com.google.android.exoplayer2.SimpleExoPlayer
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
    requested: Boolean

) : AdsLoader.AdsLoadedListener, AdErrorEvent.AdErrorListener, AdEvent.AdEventListener {
    companion object {
        lateinit var mPlayer: SimpleExoPlayer;
        lateinit var mPlayerView: PlayerView;
        lateinit var mContext: Context;
        lateinit var mAdUicontainer: ViewGroup;
        lateinit var adsLoader: AdsLoader;
        lateinit var mDaiAssetKey: String;
        var mDaiApiKey: String? =null;
        lateinit var mImaUrl: String;
        lateinit var streamManager: StreamManager;
        var mWithIma: Boolean = false;
        var mRequested: Boolean = false;
        var mAutoplay: Boolean = false;
        var mReintialize: Boolean = false;
        var playerCallbacks: MutableList<VideoStreamPlayerCallback>? = mutableListOf<VideoStreamPlayerCallback>()

    }

    init {
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
        val videoStreamPlayer: VideoStreamPlayer = createVideoStreamPlayer()!!;
        val sdkFactory = ImaSdkFactory.getInstance()
        val displayContainer = ImaSdkFactory.createStreamDisplayContainer(
            mAdUicontainer,
            videoStreamPlayer
        )
        val settings = sdkFactory.createImaSdkSettings()
        settings.autoPlayAdBreaks = true

        settings.playerType = "HibridPlayer"
        adsLoader = sdkFactory.createAdsLoader(mContext, settings, displayContainer)
        val request: StreamRequest = sdkFactory.createLiveStreamRequest(
            mDaiAssetKey,
            mDaiApiKey
        );
        adsLoader.addAdErrorListener(this)
        adsLoader.addAdsLoadedListener(this)
        adsLoader.requestStream(request)
        var myPlayer = MyPlayer();
        myPlayer.setSampleVideoPlayerCallback(object : SampleVideoPlayerCallback {
            override fun onUserTextReceived(userText: String?) {
                for (callback in playerCallbacks!!) {
                    callback.onUserTextReceived(userText)
                }
            }

            override fun onSeek(windowIndex: Int, positionMs: Long) {
                var timeToSeek = positionMs.toDouble()

            }

        })
    }

    private fun createVideoStreamPlayer(): VideoStreamPlayer? {

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

            override fun addCallback(
                videoStreamPlayerCallback: VideoStreamPlayerCallback
            ) {
                playerCallbacks!!.add(videoStreamPlayerCallback)
                    Log.e("wadih", "addCallback")
            }

            override fun removeCallback(
                videoStreamPlayerCallback: VideoStreamPlayerCallback
            ) {
                playerCallbacks!!.remove(videoStreamPlayerCallback)
                Log.e("wadih", "removeCallback")
            }

            override fun onAdBreakStarted() {
                Log.e("wadih", "onAdBreakStarted")
//                mPlayerView.hideController()
            }

            override fun onAdBreakEnded() {
//                mPlayerView.hideController()
            }

            override fun onAdPeriodStarted() {
                Log.e("wadih", "onAdPeriodStarted")
            }

            override fun onAdPeriodEnded() {
                Log.e("wadih", "onAdPeriodEnded")
            }
            override fun seek(p0: Long) {
                mPlayer.seekTo(p0)
            }
        }
    }

    fun createMediaSources(url: String) {
        if(!mRequested) {
            val defaultBandwidthMeter = DefaultBandwidthMeter()
            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                mContext,
                Util.getUserAgent(mContext, "Exo2"), defaultBandwidthMeter
            )
            val mediaSource =
                if (mWithIma && !mReintialize) {
                    ImaWrapper().init(
                        playerView = mPlayerView,
                        url = url,
                        imaUrl = mImaUrl,
                        context = mContext,
                        player = mPlayer
                    )
                } else {
                    HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(url));
                }

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
        Log.e("AdsManagerLoadedEvent", p0.toString())
    }
    override fun onAdError(p0: AdErrorEvent?) {
        Log.e("AdErrorEvent", p0.toString())
    }
    override fun onAdEvent(p0: AdEvent?) {
        Log.e("AdErrorEvent", p0.toString())
    }
}