package com.tugas_akhir.myapplication

import android.app.Application
import com.cloudinary.android.MediaManager
//db cloudinary gambar
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val cloudinaryConfig = hashMapOf(
            "cloud_name" to "dl1rz7bop", // ⬅️ TANPA SPASI (sesuai dashboard)
            "api_key" to "257185352519847",
            "api_secret" to "acSp051H-Lsy9qfpzkriI3cmeV4",
            "secure" to true
        )

        MediaManager.init(this, cloudinaryConfig)
    }
}
