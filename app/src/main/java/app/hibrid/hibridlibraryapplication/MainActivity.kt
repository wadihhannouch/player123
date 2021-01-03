package app.hibrid.hibridlibraryapplication

import android.os.Bundle
import app.hibrid.hibridplayer.HibridPlayer
import app.hibrid.hibridplayer.Utils.HibridPlayerSettings
import app.hibrid.hibridplayer.Utils.HibridActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : HibridActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val application = application as AnalyticsApplication
        val mTracker = application.getDefaultTracker()

        val settings = HibridPlayerSettings(
            channelKey = "Khaleejiya",
            autoplay = true,
            daiAssetKey = "sN_IYUG8STe1ZzhIIE_ksA",
//            daiAssetKey = "oAIUDEIWQ8ubHCUcRHxL3A",
//            daiApiKey = "1ED69721F0ED78979BB9DAC0745CF86413ECCA98BEBDCD35F3C5E1AAD8D9939C",
            imaUrl = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpreonly&cmsid=496&vid=short_onecue&correlator=",
            withIma = true,
            withDai = true,
            baseUrl = "https://live.hibridcdn.net/rotana/khaleejiya_mabr/playlist.m3u8"
        )
        HibridPlayer(
            context = this,
            hibridSettings = settings,
            gaTracker = mTracker!!,
            includeLayout = includeLayout
        )
    }

}