package app.hibrid.hibridplayer.Wrapper

import android.content.Context
import android.net.Uri
import android.os.Handler
import app.hibrid.hibridplayer.HibridPlayer
import app.hibrid.hibridplayer.Utils.HibridPlayerSettings
import app.hibrid.hibridplayer.Utils.SendGaTrackerEvent
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
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.analytics.Tracker


class ImaWrapper : MediaSourceEventListener, AdsLoader.AdsLoadedListener,
    AdErrorEvent.AdErrorListener, AdEvent.AdEventListener{
    companion object {
        var mGaTracker: Tracker? = null;
        var adLoading: AdLoading = AdLoading.None;
        lateinit var mPlayer: SimpleExoPlayer;
        lateinit var mDefaultBandwidthMeter: DefaultBandwidthMeter;
        lateinit var mPlayerView: PlayerView;
        lateinit var mChannelId : String;
        lateinit var mHibridSettings: HibridPlayerSettings;
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
        hibridSettings: HibridPlayerSettings,
        defaultBandwidthMeter: DefaultBandwidthMeter,
        channelId: String?
    ): AdsMediaSource {
        mHibridSettings = hibridSettings
        mDefaultBandwidthMeter = defaultBandwidthMeter
        mPlayer = player;
        mPlayerView = playerView;
        mGaTracker = gaTracker
        mChannelId = channelId!!
        val mImaUri = Uri.parse(imaUrl);
        val mImaAdsLoader = ImaAdsLoader.Builder(context)
            .setAdEventListener(this)
            .setAdErrorListener(this)
            .buildForAdTag(mImaUri)



        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, "Exo2"),
            mDefaultBandwidthMeter
        )

        val uri = Uri.parse(url)
        val mediaItem = MediaItem.fromUri(uri);
        val mediaSource: MediaSource =
            HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
//            SsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
        val mediaSourceFactory: ProgressiveMediaSource.Factory =
            ProgressiveMediaSource.Factory(dataSourceFactory)
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
                mPlayer.currentTimeline.getPeriod(mPlayer.currentPeriodIndex, Timeline.Period()).getAdGroupTimeUs(
                    mPlayer.currentPeriodIndex)
            if (value.toInt() == 0 && adLoading == AdLoading.None) {
                adLoading = AdLoading.Started
                SendGaTrackerEvent(mGaTracker, mChannelId,"Preroll ad","Started")
            }
        } else {
            if (adLoading == AdLoading.Started) {
                adLoading = AdLoading.None;
                SendGaTrackerEvent(mGaTracker, mChannelId,"Preroll ad","Ended")
            }
        }
        super.onLoadStarted(windowIndex, mediaPeriodId, loadEventInfo, mediaLoadData)
    }

    override fun onAdsManagerLoaded(p0: AdsManagerLoadedEvent?) {

    }

    override fun onAdError(p0: AdErrorEvent?) {
        SendGaTrackerEvent(mGaTracker, mChannelId,"Ima error", p0!!.error.message + " " +  p0!!.error.errorCodeNumber.toString())
    }



    override fun onAdEvent(event: AdEvent?) {
        when (event!!.type) {
            AdEvent.AdEventType.AD_PROGRESS -> {
            }
            AdEvent.AdEventType.CLICKED-> {
                SendGaTrackerEvent(mGaTracker, mChannelId,"ad_click","dai_ad")
            }
            else -> print(String.format("Event Type: %s\n", event.type))
        }
    }
}