package app.hibrid.hibridplayer


import android.content.Context
import android.net.Uri
import android.widget.FrameLayout
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


class DaiWrapper(
    player: SimpleExoPlayer,
    playerView: PlayerView,
    context: Context,
    adUicontainer: FrameLayout,
    daiAssetKey: String,
    daiApiKey: String,
    withIma: Boolean,
    imaUrl: String,
    autoplay: Boolean,
    reintialize: Boolean,
    requested: Boolean

) : AdsLoader.AdsLoadedListener, AdErrorEvent.AdErrorListener {
    companion object {
        lateinit var mPlayer: SimpleExoPlayer;
        lateinit var mPlayerView: PlayerView;
        lateinit var mContext: Context;
        lateinit var mAdUicontainer: FrameLayout;
        lateinit var adsLoader: AdsLoader;
        lateinit var mDaiAssetKey: String;
        lateinit var mDaiApiKey: String;
        lateinit var mImaUrl: String;
        var mWithIma: Boolean = false;
        var mRequested: Boolean = false;
        var mAutoplay: Boolean = false;
        var mReintialize: Boolean = false;
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
        val displayContainer =
            ImaSdkFactory.createStreamDisplayContainer(mAdUicontainer, videoStreamPlayer)
        val sdkFactory = ImaSdkFactory.getInstance()
        val settings = sdkFactory.createImaSdkSettings()
        // Change any settings as necessary here.
        settings.autoPlayAdBreaks = true
        settings.playerType = "PLAYER_TYPE"
        adsLoader = sdkFactory.createAdsLoader(mContext, settings, displayContainer)
        val request: StreamRequest = sdkFactory.createLiveStreamRequest(
            mDaiAssetKey,
            mDaiApiKey
        );
        adsLoader.addAdErrorListener(this)
        adsLoader.addAdsLoadedListener(this)

        var stremUrlRequest = adsLoader.requestStream(request);
        Log.d("Dai stremUrlRequest1", stremUrlRequest.toString())
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

            }

            override fun removeCallback(
                videoStreamPlayerCallback: VideoStreamPlayerCallback
            ) {

            }

            override fun onAdBreakStarted() {
                mPlayerView.hideController()
            }

            override fun onAdBreakEnded() {
                mPlayerView.showController()
                // Re-enable player controls.
            }

            override fun onAdPeriodStarted() {

            }

            override fun onAdPeriodEnded() {
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
            MyPlayer().init(player = mPlayer, mediaSource = mediaSource, playerView = mPlayerView,autoplay = mAutoplay)
            mRequested = true
        }
    }

    override fun onAdsManagerLoaded(p0: AdsManagerLoadedEvent?) {
        Log.e("AdsManagerLoadedEvent",p0.toString())
    }

    override fun onAdError(p0: AdErrorEvent?) {
        Log.e("AdErrorEvent",p0.toString())
    }

}