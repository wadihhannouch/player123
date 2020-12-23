package app.hibrid.hibridplayer

import android.os.Handler
import com.google.ads.interactivemedia.v3.api.AdsLoader
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.util.Log

class ImaHibridWrapper {
        fun init(
            dataSourceFactory: DataSource.Factory,
            mediaSource: MediaSource,
            playerView: PlayerView,
            imaAdsLoader: ImaAdsLoader
        ): AdsMediaSource {
            val mediaSourceFactory: ProgressiveMediaSource.Factory =
                ProgressiveMediaSource.Factory(dataSourceFactory)
            Log.d("Hibrid Player", "mediaSourceFactory created");
            val adsMediaSource = AdsMediaSource(
                mediaSource,
                mediaSourceFactory,
                SmoothStreaminHibridPlayer.mImaAdsLoader,
                playerView
            )
//            adsMediaSource.addEventListener(Handler(), this)
//            playerView.player!!.addListener(this)
            return adsMediaSource;
        }

}