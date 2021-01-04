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
            daiAssetKey = "daiassetkey",
            daiApiKey = "daiapikey",
            imaUrl = "imaUrl",
            withIma = true,
            withDai = true,
            baseUrl = "baseUrl"
        )
        HibridPlayer(
            context = this,
            hibridSettings = settings,
            gaTracker = mTracker!!,
            includeLayout = includeLayout
        )
    }

}