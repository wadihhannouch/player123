package app.hibrid.hibridlibraryapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.hibrid.hibridplayer.HibridPlayer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        HibridPlayer(
            urlStreaming = "https://live.hibridcdn.net/rotana/khaleejiya_mabr/playlist.m3u8",
            playerView = hibridPlayerView,
            context = this,
            withIma = true,
            withDai = true,
            imaUrl ="![CDATA[https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator=]]",
            adUicontainer = adUiContainer
        );
    }
}