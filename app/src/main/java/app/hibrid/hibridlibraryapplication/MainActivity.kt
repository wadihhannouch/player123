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
            channelKey = "rotana-cinema",
            lisence = "MvbyQ6F4Lr2s3FU6ZMgHT92stjkFg8qeNLJwF5FJh5tJauQennNFjyaUQywdrwGR"
        )

        HibridPlayer(
            context = this,
            hibridSettings = settings,
            includeLayout = includeLayout,
            application = myApplication
        )
    }
}