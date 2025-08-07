package edu.bluejack24_2.ecoai

import android.app.Application
import edu.bluejack24_2.ecoai.utils.NotificationRealtimeListener

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationRealtimeListener().start(this)
    }
}
