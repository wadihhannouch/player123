package app.hibrid.hibridplayer.Utils

import com.google.android.exoplayer2.util.Log
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker

class SendGaTrackerEvent (mGaTracker:Tracker?, channelKey:String, title:String, description:String) {
    init {
        mGaTracker?.send(
            HitBuilders.EventBuilder()
                .setCategory(channelKey).setAction(title).setLabel(description)
                .build()
        );
    }

}