package app.hibrid.hibridplayer

import android.content.Context
import android.net.Uri
import android.widget.FrameLayout
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.ui.PlayerView


class HibridPlayer(
    urlStreaming: String,
    playerView: PlayerView,
    context: Context,
    withIma: Boolean,
    withDai: Boolean,
    imaUrl: String,
    adUicontainer: FrameLayout
){
    companion object{
        lateinit var mImaAdsLoader:ImaAdsLoader;
        lateinit var mUrlStreaming: String;
        lateinit var mPlayerView:PlayerView;
        lateinit var mContext:Context;
        lateinit var mImaUrl:String;
        lateinit var mAdUicontainer:FrameLayout;
        var mWithIma:Boolean = false;
    }
    init {
        mUrlStreaming = urlStreaming;
        mPlayerView = playerView;
        mContext = context;
        mImaUrl = imaUrl;
        mAdUicontainer = adUicontainer
        mWithIma = withIma;

        initialize(mUrlStreaming, mPlayerView, mContext,mWithIma)
    }
    fun initialize (
        urlStreaming: String,
        playerView: PlayerView,
        context: Context,
        mWithIma: Boolean
    ){
            if(mWithIma){
                val uri =  Uri.parse(mImaUrl);
                mImaAdsLoader = ImaAdsLoader(context, uri)
            }

        SmoothStreaminHibridPlayer(
            urlStreaming = urlStreaming,
            playerView = playerView,
            context = context,
            withIma = true,
            withDaiIma = true,
            imaAdsLoader = mImaAdsLoader,
            adUicontainer = mAdUicontainer
        );
    }
}