package app.hibrid.hibridplayer

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.BehindLiveWindowException
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy


class HibridPlayer(
    urlStreaming: String,
    playerView: PlayerView,
    context: Context,
    withIma: Boolean,
    withDai: Boolean,
    imaUrl: String,
    adUicontainer: FrameLayout,
    daiAssetKey: String,
    daiApiKey: String?,
    autoplay: Boolean
) : Player.EventListener {
    companion object {
        fun pause() {
            mPlayerView.keepScreenOn = false
            player.playWhenReady= false
        }
        fun play() {
            mPlayerView.keepScreenOn = true
            player.playWhenReady= true
        }

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
        lateinit var sampleVideoPlayer :VideoPlayer;
        lateinit var player :SimpleExoPlayer ;
    }

    init {
        var cookieManager: CookieManager? = null
        cookieManager =  CookieManager ()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER)
        if (CookieHandler.getDefault() !== cookieManager) {
            CookieHandler.setDefault(cookieManager)
        }

        mUrlStreaming = urlStreaming;
        mPlayerView = playerView;
        mContext = context;
        mImaUrl = imaUrl;
        mAdUicontainer = adUicontainer
        mWithIma = withIma;
        mWithDai = withDai
        mdaiApiKey = daiApiKey
        mDaiAssetKey = daiAssetKey
        mAutoplay = autoplay


        initialize(reintialize = false)
    }

    fun initialize(reintialize: Boolean) {
        player = SimpleExoPlayer.Builder(mContext).build()
//        player.addListener(this)

        if (mWithDai) {

           sampleVideoPlayer = VideoPlayer(
               mContext, mPlayerView, mWithIma, mImaUrl
           )
            sampleVideoPlayer.enableControls(false)
            val sampleAdsWrapper = DaiAdsWrapper(
                mContext,
                sampleVideoPlayer,
                mAdUicontainer, mWithIma, mImaUrl, mDaiAssetKey, mdaiApiKey
            )
            sampleAdsWrapper.requestAndPlayAds()
//            sampleAdsWrapper.setFallbackUrl(mUrlStreaming)

//            DaiWrapper(
//                requested = false,
//                player = player,
//                playerView = mPlayerView,
//                context = mContext,
//                adUicontainer = mAdUicontainer,
//                daiApiKey = mdaiApiKey,
//                daiAssetKey = mDaiAssetKey,
//                withIma = mWithIma,
//                imaUrl = mImaUrl,
//                autoplay = mAutoplay,
//                reintialize = reintialize,
//                streamUrl = mUrlStreaming
//            )
        }
        else {
            val defaultBandwidthMeter = DefaultBandwidthMeter()
            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                mContext,
                Util.getUserAgent(mContext, "Exo2"), defaultBandwidthMeter
            )
            val url = if(mWithIma && !reintialize) {
                mImaUrl} else mUrlStreaming
            mMediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(
                Uri.parse(
                    url
                )
            );
            MyPlayer().init(
                player = player,
                mediaSource = mMediaSource,
                playerView = mPlayerView,
                autoplay = mAutoplay
            )
        }
    }
    override fun onPlayerError(error: ExoPlaybackException) {
        Log.e("PLAYER_ACTIVITY_TAG", "SOME ERROR IN PLAYER");
        if (isBehindLiveWindow(error)) {
//            player.release()
            initialize(reintialize = true)
//            init(player = mPlayer,mediaSource = mMediaSource,playerView = mPlayerView)
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