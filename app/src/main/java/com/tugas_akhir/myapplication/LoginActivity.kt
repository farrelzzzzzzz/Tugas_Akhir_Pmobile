package com.tugas_akhir.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_main)

        val edUsername = findViewById<EditText>(R.id.edUsername)
        val edPassword = findViewById<EditText>(R.id.edPassword)
        val btnLogin = findViewById<AppCompatButton>(R.id.btnLogin)
        val btnSignIn = findViewById<AppCompatButton>(R.id.btnSignIn)

        // ===============================
        // 🔐 FITUR HINT DINAMIS PASSWORD
        // ===============================
        val passwordHint = "Masukkan kata sandi anda"
        edPassword.hint = passwordHint

        edPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    edPassword.hint = passwordHint
                } else {
                    edPassword.hint = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        // ===============================

        // Tombol LOGIN
        btnLogin.setOnClickListener {
            val username = edUsername.text.toString().trim()
            val password = edPassword.text.toString().trim()

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

            Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()

            // Contoh pindah ke halaman utama
            // startActivity(Intent(this, MainActivity::class.java))
            // finish()
        }

        // Tombol SIGN IN
        btnSignIn.setOnClickListener {
            Toast.makeText(this, "Menu Sign In", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, SignActivity::class.java)
            startActivity(intent)

            // Contoh pindah ke halaman register
            // startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
