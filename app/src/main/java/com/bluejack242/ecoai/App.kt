package com.bluejack242.ecoai

import android.app.Application
import com.bluejack242.ecoai.utils.NotificationRealtimeListener

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationRealtimeListener().start(this)
    }
}
