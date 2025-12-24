package com.tugas_akhir.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText

class SignActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_main)

        val edUsername = findViewById<EditText>(R.id.edUsername)
        val edPassword = findViewById<TextInputEditText>(R.id.edPassword)
        val edkonPassword = findViewById<TextInputEditText>(R.id.edkonPassword)
        val btnSignIn = findViewById<AppCompatButton>(R.id.btnSignIn)
        val btnLogin = findViewById<AppCompatButton>(R.id.btnLogin)
        // ===============================
        // 🔐 FITUR HINT DINAMIS PASSWORD
        // ===============================
        val passwordHint = "Masukkan Password anda"
        edPassword.hint = passwordHint
        edkonPassword.hint = passwordHint



        // Tombol SIGN IN
        btnSignIn.setOnClickListener {
            val username = edUsername.text.toString().trim()
            val password = edPassword.text.toString().trim()
            val konfirmasiPassword = edkonPassword.text.toString().trim()

            if (username.isEmpty()) {
                edUsername.error = "Username tidak boleh kosong"
                edUsername.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                edPassword.error = "Password tidak boleh kosong"
                edPassword.requestFocus()
                return@setOnClickListener
            }

            if (konfirmasiPassword.isEmpty()) {
                edkonPassword.error = "Konfirmasi password tidak boleh kosong"
                edkonPassword.requestFocus()
                return@setOnClickListener
            }

            if (password != konfirmasiPassword) {
                edkonPassword.error = "Password tidak cocok"
                edkonPassword.requestFocus()
                return@setOnClickListener
            }

            // TODO: Tambahkan logika untuk menyimpan data pendaftaran (misalnya ke database)

            Toast.makeText(this, "Pendaftaran berhasil", Toast.LENGTH_SHORT).show()

            // Kembali ke halaman Login setelah pendaftaran berhasil
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Menutup SignActivity agar tidak bisa kembali dengan tombol back
        }

        // Tombol LOGIN (kembali ke halaman login)
        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
