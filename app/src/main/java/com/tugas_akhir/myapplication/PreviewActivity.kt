package com.tugas_akhir.myapplication

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class PreviewActivity : AppCompatActivity() {

    private lateinit var imgPreview: ImageView
    private lateinit var btnUpload: Button
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_main)

        // SESUAI XML
        imgPreview = findViewById(R.id.imgPreview)
        btnUpload = findViewById(R.id.btnUpload)
        btnBack = findViewById(R.id.btnBack)

        // Ambil path foto dari MenuActivity
        val photoPath = intent.getStringExtra("photo_path")

        if (!photoPath.isNullOrEmpty()) {
            val file = File(photoPath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                imgPreview.setImageBitmap(bitmap)
            } else {
                Toast.makeText(this, "File foto tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Path foto kosong", Toast.LENGTH_SHORT).show()
        }

        // Tombol upload (dummy)
        btnUpload.setOnClickListener {
            Toast.makeText(this, "Upload foto (belum diimplementasi)", Toast.LENGTH_SHORT).show()
        }

        // Tombol back di UI
        btnBack.setOnClickListener {
            finish()
        }
    }

    // Back sistem (gesture / tombol HP)
    override fun onBackPressed() {
        finish()
    }
}
