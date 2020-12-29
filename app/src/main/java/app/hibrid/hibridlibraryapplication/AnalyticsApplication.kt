package app.hibrid.hibridlibraryapplication

import android.R
import android.app.Application
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker


class AnalyticsApplication : Application() {
    private var sAnalytics: GoogleAnalytics? = null
    private var sTracker: Tracker? = null

    override fun onCreate() {
        super.onCreate()
        sAnalytics = GoogleAnalytics.getInstance(this)
    }
    /**
     * Gets the default [Tracker] for this [Application].
     * @return tracker
     */
    @Synchronized
    fun getDefaultTracker(): Tracker? {
        if (sTracker == null) {
            sTracker = sAnalytics!!.newTracker("UA-61148841-2");
        }
        return sTracker
    }
}