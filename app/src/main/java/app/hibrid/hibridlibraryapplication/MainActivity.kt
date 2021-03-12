package app.hibrid.hibridlibraryapplication

import android.os.Bundle
import app.hibrid.hibridplayer.HibridPlayer
import app.hibrid.hibridplayer.Utils.HibridPlayerSettings
import app.hibrid.hibridplayer.Utils.HibridActivity
import app.hibrid.hibridplayer.Utils.HibridApplication
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : HibridActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myApplication = application as HibridApplication
        val settings = HibridPlayerSettings(
            channelKey = "TEstKey",
            autoplay = true,
            daiAssetKey = "sN_IYUG8STe1ZzhIIE_ksA",
            daiApiKey = "",
            imaUrl = "https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpreonly&url=https://developers.google.com/&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=5776&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0",
            withIma = true,
            withDai = false,
            baseUrl = "https://demo.unified-streaming.com/video/tears-of-steel/tears-of-steel.ism/.m3u8"
        )

        HibridPlayer(
            context = this,
            hibridSettings = settings,
            includeLayout = includeLayout,
            application = myApplication
        )
    }
}