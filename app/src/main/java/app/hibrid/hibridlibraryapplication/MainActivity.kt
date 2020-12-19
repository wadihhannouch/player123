package app.hibrid.hibridlibraryapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.hibrid.hibridplayer.HibridPlayer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        HibridPlayer.init(urlStreaming = "https://live.hibridcdn.net/rotana/khaleejiya_mabr/playlist.m3u8",playerView = hibridPlayerView,context = this);

    }

}