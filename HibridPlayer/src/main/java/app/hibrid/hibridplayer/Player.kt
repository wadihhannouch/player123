package app.hibrid.hibridplayer

import android.R
import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.ui.PlayerView


class HibridPlayer(urlStreaming: String, playerView:PlayerView, context:Context){
    companion object{
        lateinit var mImaAdsLoader:ImaAdsLoader;
        lateinit var mUrlStreaming: String;
        lateinit var mPlayerView:PlayerView;
        lateinit var mContext:Context;
    }
    init {
        mUrlStreaming = urlStreaming;
        mPlayerView = playerView;
        mContext = context;
        initialize(mUrlStreaming, mPlayerView, mContext)
    }

    fun initialize (urlStreaming: String, playerView:PlayerView, context:Context){

        val uri =  Uri.parse("![CDATA[https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator=]]");
        try {
            mImaAdsLoader = ImaAdsLoader(context, uri)
        }catch(e:Exception){
            e.toString();
        }

        SmoothStreaminHibridPlayer(
            urlStreaming = urlStreaming,
            playerView = playerView,
            context = context,
            withIma = true,
            imaAdsLoader = mImaAdsLoader
        );
    }
}