package app.hibrid.hibridplayer

import android.content.Context
import android.net.Uri
import android.widget.FrameLayout
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
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


class SmoothStreaminHibridPlayer(
    urlStreaming: String, playerView: PlayerView, context: Context,
    withIma: Boolean,
    withDaiIma: Boolean,
    imaAdsLoader: ImaAdsLoader,
    adUicontainer: FrameLayout
) : Player.EventListener, VideoListener, MediaSourceEventListener {
        companion object {
            lateinit var mUrl: String
            lateinit var mPlayerView: PlayerView;
            lateinit var mContext: Context;
            lateinit var mImaAdsLoader: ImaAdsLoader;
            lateinit var mAdUicontainer: FrameLayout
            var mWithIma: Boolean = false;
            var mWithDaiIma: Boolean = false;
        }

        init {
            mUrl = urlStreaming;
            mPlayerView = playerView
            mContext = context
            mImaAdsLoader = imaAdsLoader;
            mWithIma = withIma;
            mWithDaiIma = withDaiIma;
            mAdUicontainer =  adUicontainer;
            init(urlStreaming, playerView, context, mWithIma)
        }

        fun init(urlStreaming: String, playerView: PlayerView, context: Context, mWithIma: Boolean) {
            val adaptiveTrackSelection: TrackSelection.Factory =
                AdaptiveTrackSelection.Factory()
            var player = ExoPlayerFactory.newSimpleInstance(
                context,
                DefaultRenderersFactory(context),
                DefaultTrackSelector(adaptiveTrackSelection)
            )

            val defaultBandwidthMeter = DefaultBandwidthMeter()
            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, "Exo2"), defaultBandwidthMeter
            )

            val uri = Uri.parse(urlStreaming)
            val mediaSource: MediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(
                uri
            );
            val myMediaSource :MediaSource = if(mWithIma){
                ImaHibridWrapper().init(
                    dataSourceFactory = dataSourceFactory,
                    mediaSource = mediaSource,
                    playerView = playerView,
                    imaAdsLoader = mImaAdsLoader
                )
            } else{
                mediaSource;
            }

            MyPlayer().init(player,
                myMediaSource,
                playerView,
                this,
                mWithDaiIma,
                "oAIUDEIWQ8ubHCUcRHxL3A",
                "1ED69721F0ED78979BB9DAC0745CF86413ECCA98BEBDCD35F3C5E1AAD8D9939C",
                mAdUicontainer,
                context,
                uri,
                mWithIma)
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            Log.e("PLAYER_ACTIVITY_TAG", "SOME ERROR IN PLAYER");
            if (isBehindLiveWindow(error)) {
                init(mUrl, mPlayerView, mContext, mWithIma);
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