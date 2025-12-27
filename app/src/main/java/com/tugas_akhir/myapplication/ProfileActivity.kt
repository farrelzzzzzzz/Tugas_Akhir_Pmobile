package com.tugas_akhir.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_main)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnEditProfile = findViewById<LinearLayout>(R.id.btnEditProfile)
        val btnBuatPostingan = findViewById<Button>(R.id.btnBuatPostingan)

        // BACK
        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
        }

        // EDIT PROFILE
        btnEditProfile.setOnClickListener {
            Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show()
        }

        // BUAT POSTINGAN (arah seperti back)
        btnBuatPostingan.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)

            // animasi SEARAH back
            overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
        }
    }
}
