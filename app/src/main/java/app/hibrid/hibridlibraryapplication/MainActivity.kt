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
            channelKey = "Khaleejiya",
            autoplay = true,
            daiAssetKey = "oAIUDEIWQ8ubHCUcRHxL3A",
            daiApiKey = "1ED69721F0ED78979BB9DAC0745CF86413ECCA98BEBDCD35F3C5E1AAD8D9939C",
            imaUrl = "https://tinyurl.com/y8ygf7qn",
            withIma = true,
            withDai = true,
            baseUrl = "https://multiplatform-f.akamaihd.net/i/multi/will/bunny/big_buck_bunny_,640x360_400,640x360_700,640x360_1000,950x540_1500,.f4v.csmil/master.m3u8"
        )

        HibridPlayer(
            context = this,
            hibridSettings = settings,
            includeLayout = includeLayout,
            application = myApplication
        )
    }

}