package app.hibrid.hibridplayer

import android.content.Context
import android.net.Uri
import android.os.Handler
import com.google.ads.interactivemedia.v3.api.*
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import kotlin.properties.Delegates

class ImaWrapper : MediaSourceEventListener, AdsLoader.AdsLoadedListener,
    AdErrorEvent.AdErrorListener {
    companion object {
        var mGaTracker: Tracker? = null;
        var mwithGaTracker: Boolean = false;
        var adLoading: AdLoading = AdLoading.None;
        lateinit var mPlayer: SimpleExoPlayer;
        lateinit var mPlayerView: PlayerView;
    }

    enum class AdLoading {
        None,
        Started,
    }

    fun init(
        url: String,
        playerView: PlayerView,
        player: SimpleExoPlayer,
        imaUrl: String,
        context: Context,
        gaTracker: Tracker?,
        withGaTracker: Boolean
    ): AdsMediaSource {
        mPlayer = player;
        mPlayerView = playerView;
        mwithGaTracker = withGaTracker
        mGaTracker = gaTracker
        val mImaUri = Uri.parse(imaUrl);
        val mImaAdsLoader = ImaAdsLoader.Builder(context).buildForAdTag(mImaUri)
        val defaultBandwidthMeter = DefaultBandwidthMeter.Builder(context).build()
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, "Exo2"),
            defaultBandwidthMeter
        )
        val uri = Uri.parse(url)

        val mediaItem = MediaItem.fromUri(uri);
        val mediaSource: MediaSource =
            HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
        val mediaSourceFactory: ProgressiveMediaSource.Factory =
            ProgressiveMediaSource.Factory(dataSourceFactory)

        Log.d("Hibrid Player", "mediaSourceFactory created");

        val adsMediaSource =
            AdsMediaSource(
                mediaSource,
                mediaSourceFactory,
                mImaAdsLoader,
                playerView
            )
        adsMediaSource.addEventListener(Handler(), this)
        val sdkFactory = ImaSdkFactory.getInstance()
        val settings = sdkFactory.createImaSdkSettings()
        settings.playerType = "Ima Preroll"

        val adsLoader =
            sdkFactory.createAdsLoader(context, settings, mImaAdsLoader.adDisplayContainer)
        adsLoader.addAdsLoadedListener(this)
        mImaAdsLoader.setPlayer(player)
        return adsMediaSource;
    }

    override fun onLoadStarted(
        windowIndex: Int,
        mediaPeriodId: MediaSource.MediaPeriodId?,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        if (mPlayer.isPlayingAd) {
            val value =
                mPlayer.currentTimeline.getPeriod(mPlayer.currentPeriodIndex, Timeline.Period())
                    .getAdGroupTimeUs(
                        mPlayer.currentPeriodIndex
                    )
            if (value.toInt() == 0 && adLoading == AdLoading.None) {

                adLoading = AdLoading.Started
                sendGaTrackerEvent(title = "Preroll ad", description = "Started")
            }
        } else {
            if (adLoading == AdLoading.Started) {
                adLoading = AdLoading.None;
                sendGaTrackerEvent(title = "Preroll ad", description = "Ended")

            }
        }
        super.onLoadStarted(windowIndex, mediaPeriodId, loadEventInfo, mediaLoadData)
    }

    override fun onAdsManagerLoaded(p0: AdsManagerLoadedEvent?) {

    }

    override fun onAdError(p0: AdErrorEvent?) {

    }

    fun sendGaTrackerEvent(title: String, description: String) {
        if (mwithGaTracker && mGaTracker != null)
            mGaTracker!!.send(
                HitBuilders.EventBuilder()
                    .setCategory(title).setCategory(description)
                    .build()
            );
        Log.d(title ,description);
    }


}