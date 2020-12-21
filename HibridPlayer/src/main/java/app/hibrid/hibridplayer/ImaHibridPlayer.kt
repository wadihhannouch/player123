package app.hibrid.hibridplayer

import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.util.Log

class ImaHibridPlayer {
    fun init(
        dataSourceFactory: DataSource.Factory,
        mediaSource: MediaSource,
        playerView: PlayerView
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
        return adsMediaSource;
    }

}