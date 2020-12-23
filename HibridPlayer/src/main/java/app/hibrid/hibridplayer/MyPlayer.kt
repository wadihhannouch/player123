package app.hibrid.hibridplayer

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.widget.FrameLayout
import com.google.android.exoplayer2.Player.EventListener
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.Log
import java.lang.Exception

class MyPlayer(

) : EventListener {
    companion object {
        lateinit var mPlayer: SimpleExoPlayer;
        lateinit var mMediaSource: MediaSource;
        lateinit var mPlayerView: PlayerView;
        lateinit var mSmoothStreaminHibridPlayer: SmoothStreaminHibridPlayer;
        var mWithDaiIma: Boolean = false;
        lateinit var mDaiAssetKey: String;
        lateinit var mDaiApiKey: String;
        lateinit var mAdUicontainer: FrameLayout;
        lateinit var mContext: Context;
        lateinit var mUri: Uri;
        var mWithIma: Boolean = false
    }

    fun init(
        player: SimpleExoPlayer,
        mediaSource: MediaSource,
        playerView: PlayerView,
        smoothStreaminHibridPlayer: SmoothStreaminHibridPlayer,
        withDaiIma: Boolean,
        daiAssetKey: String,
        daiApiKey: String,
        adUicontainer: FrameLayout,
        context: Context,
        uri: Uri,
        withIma: Boolean
    ) {
        mPlayer = player
        mMediaSource = mediaSource
        mPlayerView = playerView
        mSmoothStreaminHibridPlayer = smoothStreaminHibridPlayer
        mWithDaiIma = withDaiIma
        mDaiAssetKey = daiAssetKey
        mDaiApiKey = daiApiKey
        mAdUicontainer = adUicontainer
        mContext = context
        mUri = uri
        mWithIma = withIma
        if (mWithDaiIma) {
            try {
                DaiWrapper(mPlayer,context =context,playerView = playerView,adUicontainer = mAdUicontainer,uri = mUri,mMediaSource = mMediaSource);
            } catch (e: Exception) {
                Log.d("Excception dai", e.toString())
            }
        }
        mPlayer.addMediaSource(mMediaSource);
        mPlayer.addListener(mSmoothStreaminHibridPlayer)
        mPlayer.addVideoListener(mSmoothStreaminHibridPlayer)
        mMediaSource.addEventListener(Handler(), mSmoothStreaminHibridPlayer)
        mPlayerView.player = mPlayer
        SmoothStreaminHibridPlayer.mImaAdsLoader.setPlayer(mPlayer);
        mPlayer.playWhenReady = true
        mPlayer.prepare()
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        super.onTimelineChanged(timeline, reason)
    }


}