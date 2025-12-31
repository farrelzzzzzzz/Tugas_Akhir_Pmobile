package com.tugas_akhir.myapplication

import com.cloudinary.android.MediaManager

object CloudinaryDeleteUtil {

    fun deleteAsync(publicId: String, onResult: (Boolean) -> Unit) {
        Thread {
            try {
                val result = MediaManager.get()
                    .cloudinary
                    .uploader()
                    .destroy(publicId, mapOf("invalidate" to true))

                val success = result["result"] == "ok"

                onResult(success)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }.start()
    }
}
