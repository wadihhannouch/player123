package app.hibrid.hibridplayer

import android.content.Context
import com.google.android.exoplayer2.ui.PlayerView


object HibridPlayer{
    fun init (urlStreaming: String, playerView:PlayerView, context:Context){
        SmoothStreaminHibridPlayer(urlStreaming = urlStreaming,playerView = playerView,context = context);
    }
}