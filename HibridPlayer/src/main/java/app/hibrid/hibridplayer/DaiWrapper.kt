package app.hibrid.hibridplayer


import android.content.Context
import android.net.Uri
import android.widget.FrameLayout
import com.google.ads.interactivemedia.v3.api.*
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate
import com.google.ads.interactivemedia.v3.api.player.VideoStreamPlayer
import com.google.ads.interactivemedia.v3.api.player.VideoStreamPlayer.VideoStreamPlayerCallback
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.common.net.HttpHeaders.USER_AGENT
import com.google.ads.interactivemedia.v3.api.AdsLoader
import com.google.ads.interactivemedia.v3.api.StreamRequest
import com.google.android.exoplayer2.util.Log


class DaiWrapper(
    player: SimpleExoPlayer,
    playerView: PlayerView,
    context: Context,
    adUicontainer: FrameLayout,
    uri: Uri,
    mediaSource: MediaSource
) : AdsLoader.AdsLoadedListener {
    companion object{
        lateinit var mPlayer:SimpleExoPlayer;
        lateinit var mPlayerView:PlayerView;
        lateinit var mContext:Context;
        lateinit var mAdUicontainer: FrameLayout;
        lateinit var mUri: Uri;
        lateinit var adsLoader: AdsLoader;
        lateinit var mMediaSource: MediaSource;
    }
    init{
        mPlayer = player
        mPlayerView = playerView
        mContext = context;
        mAdUicontainer = adUicontainer;
        mUri = uri;
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
        val request :StreamRequest = sdkFactory.createLiveStreamRequest(MyPlayer.mDaiAssetKey, MyPlayer.mDaiApiKey);
        var stremUrlRequest = adsLoader.requestStream(request);
        Log.d("Dai stremUrlRequest1" ,stremUrlRequest)
        Log.d("Dai requestcontentUrl" ,request.contentUrl)

        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(mContext, USER_AGENT)
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
        mediaSourceFactory.setAdViewProvider(mPlayerView)
        adsLoader.addAdsLoadedListener(this)
//        mMediaSource
//        val mediaItem: MediaItem =MediaItem.Builder().setUri(mUri).build()
//        val mediaSource: MediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(
//            mediaItem
//        )
//        return mediaSource;
        }


    private fun createVideoStreamPlayer(): VideoStreamPlayer? {
        return object : VideoStreamPlayer {
            override fun getContentProgress(): VideoProgressUpdate {
                mPlayer.play()
                return VideoProgressUpdate(
                    mPlayer.bufferedPosition, mPlayer.duration);
            }

            override fun getVolume(): Int {
                return 100
            }

            override fun loadUrl(url: String?, subtitles: List<HashMap<String?, String?>?>?) {
                Log.d("Dai Url" ,url!!)
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

    override fun onAdsManagerLoaded(p0: AdsManagerLoadedEvent?) {
        Log.d("AdsManagerLoadedEvent" , p0.toString())
    }

}