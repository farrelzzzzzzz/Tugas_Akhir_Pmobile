package com.tugas_akhir.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val edUsername = findViewById<EditText>(R.id.edUsername)
        val edPassword = findViewById<TextInputEditText>(R.id.edPassword)
        val edkonPassword = findViewById<TextInputEditText>(R.id.edkonPassword)
        val btnSignIn = findViewById<AppCompatButton>(R.id.btnSignIn)
        val btnLogin = findViewById<AppCompatButton>(R.id.btnLogin)

        btnSignIn.setOnClickListener {
            val email = edUsername.text.toString().trim()
            val password = edPassword.text.toString().trim()
            val konfirmasiPassword = edkonPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || konfirmasiPassword.isEmpty()) {
                Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != konfirmasiPassword) {
                Toast.makeText(this, "Password tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔐 SIGN UP FIREBASE AUTH
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val userId = auth.currentUser!!.uid

                        // Data user (TANPA password)
                        val userMap = mapOf(
                            "email" to email,
                            "role" to "user"
                        )

                        // Simpan ke Realtime Database
                        database.reference.child("users")
                            .child(userId)
                            .setValue(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Pendaftaran berhasil",
                                    Toast.LENGTH_SHORT
                                ).show()

                                startActivity(
                                    Intent(this, LoginActivity::class.java)
                                )
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "Gagal menyimpan data",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                    } else {
                        Toast.makeText(
                            this,
                            task.exception?.message ?: "Registrasi gagal",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
