package com.tugas_akhir.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class Halaman1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sesuai nama XML
        setContentView(R.layout.halaman1_main)

        val btnStart = findViewById<LinearLayout>(R.id.btnStart)

        btnStart.setOnClickListener {
            // Pindah ke halaman berikutnya
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}