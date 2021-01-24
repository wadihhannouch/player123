package app.hibrid.hibridplayer.Utils

import android.app.Application
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker


open class HibridApplication : Application() {
        private var sAnalytics: GoogleAnalytics? = null
        private var sTracker: Tracker? = null
        private var appTracker: Tracker? = null

        override fun onCreate() {
            super.onCreate()
            sAnalytics = GoogleAnalytics.getInstance(this)
        }
        /**
         * Gets the default [Tracker] for this [Application].
         * @return tracker
         */

        @Synchronized
        fun getDefaultTracker(id: String): Tracker? {
            if (sTracker == null) {
                sTracker = sAnalytics!!.newTracker(id);
            }
            return sTracker
        }

        @Synchronized
        fun getHibridTracker(id:String): Tracker? {
            if (sTracker == null) {
                appTracker = sAnalytics!!.newTracker(id);
            }
            return sTracker
        }
//    "UA-61148841-2"
}