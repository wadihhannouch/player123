package app.hibrid.hibridplayer.Utils

import androidx.fragment.app.FragmentActivity
import app.hibrid.hibridplayer.HibridPlayer

class HibridFragment : FragmentActivity() {
    override fun onResume() {
        HibridPlayer.play()
        super.onResume()
    }

    override fun onStart() {
        HibridPlayer.play()
        super.onStart()
    }

    override fun onStop() {
        HibridPlayer.destroy()
        super.onStop()
    }
}