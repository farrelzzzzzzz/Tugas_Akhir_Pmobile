package com.tugas_akhir.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ⛔ MATIKAN EDGE-TO-EDGE ANDROID 15
        WindowCompat.setDecorFitsSystemWindows(window, true)
    }
}
