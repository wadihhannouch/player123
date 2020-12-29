package app.hibrid.hibridlibraryapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.hibrid.hibridplayer.HibridPlayer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val application = application as AnalyticsApplication
        val mTracker = application.getDefaultTracker()
        HibridPlayer(
            withGaTracker = false,
            gaTracker = null,
            context = this,
            playerView = includeLayout.findViewById(R.id.hibridPlayerView),
            adUicontainer = includeLayout.findViewById(R.id.adUiContainer),
            withIma = false,
            withDai = true,
            autoplay = true,
            urlStreaming = "https://live.hibridcdn.net/rotana/khaleejiya_mabr/playlist.m3u8",
            daiAssetKey = "oAIUDEIWQ8ubHCUcRHxL3A",
            daiApiKey = "1ED69721F0ED78979BB9DAC0745CF86413ECCA98BEBDCD35F3C5E1AAD8D9939C",
            imaUrl = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpreonly&cmsid=496&vid=short_onecue&correlator="
//            imaUrl ="https://tinyurl.com/y8ygf7qn"
        )
    }

    override fun onPause() {
        HibridPlayer.pause();
        super.onPause()
    }

    override fun onResume() {
            HibridPlayer.play()
        super.onResume()
    }

}