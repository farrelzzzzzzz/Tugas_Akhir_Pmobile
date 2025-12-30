package com.tugas_akhir.myapplication

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val config = mapOf(
            "database foto Luma" to "ISI_CLOUD_NAME_KAMU",
            "257185352519847" to "ISI_API_KEY_KAMU",
            "acSp051H-Lsy9qfpzkriI3cmeV4" to "ISI_API_SECRET_KAMU"
        )

        MediaManager.init(this, config)
    }
}
