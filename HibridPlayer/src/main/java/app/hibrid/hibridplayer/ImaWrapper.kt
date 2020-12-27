package app.hibrid.hibridplayer

import android.content.Context
import android.net.Uri
import android.os.Handler
import com.google.ads.interactivemedia.v3.api.AdsLoader
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
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

class ImaWrapper {
        fun init(
            url : String,
            playerView: PlayerView,
            player:SimpleExoPlayer,
            imaUrl:String,
            context: Context
        ): AdsMediaSource {
            val mImaUri = Uri.parse(imaUrl);
            val mImaAdsLoader = ImaAdsLoader(context, mImaUri)
            val defaultBandwidthMeter = DefaultBandwidthMeter()
            val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, "Exo2"), defaultBandwidthMeter
            )
            val uri = Uri.parse(url)
            val mediaSource: MediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(
                uri
            );
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
            mImaAdsLoader.setPlayer(player)
            return adsMediaSource;
        }
}