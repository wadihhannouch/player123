package app.hibrid.hibridplayer.Utils

import android.content.res.Configuration
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import app.hibrid.hibridplayer.HibridPlayer

open class HibridActivity : AppCompatActivity() {

    override fun onPause() {
        HibridPlayer.pause();
        super.onPause()
    }

    override fun onResume() {
        HibridPlayer.play()
        super.onResume()
    }

    override fun onDestroy() {
        HibridPlayer.destroy()
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        HibridPlayer.onConfigurationChanged(newConfig)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        HibridPlayer.getVolume()
        return super.onKeyUp(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        HibridPlayer.getVolume()
        return super.onKeyDown(keyCode, event)
    }
}