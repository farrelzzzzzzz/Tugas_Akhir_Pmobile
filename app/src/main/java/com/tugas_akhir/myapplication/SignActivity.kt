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
        val edKonfirmasi = findViewById<TextInputEditText>(R.id.edkonPassword)
        val btnSignIn = findViewById<AppCompatButton>(R.id.btnSignIn)
        val btnLogin = findViewById<AppCompatButton>(R.id.btnLogin)

        val passwordHint = "Masukkan kata sandi anda"
        edPassword.hint = passwordHint
        edKonfirmasi.hint = passwordHint

        btnSignIn.setOnClickListener {

            val email = edUsername.text.toString().trim()
            val password = edPassword.text.toString().trim()
            val konfirmasi = edKonfirmasi.text.toString().trim()

            // ================= VALIDASI =================
            if (email.isEmpty()) {
                edUsername.error = "Email tidak boleh kosong"
                edUsername.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                edPassword.error = "Password tidak boleh kosong"
                edPassword.requestFocus()
                return@setOnClickListener
            }

            if (konfirmasi.isEmpty()) {
                edKonfirmasi.error = "Konfirmasi password kosong"
                edKonfirmasi.requestFocus()
                return@setOnClickListener
            }

            if (password != konfirmasi) {
                edKonfirmasi.error = "Password tidak cocok"
                edKonfirmasi.requestFocus()
                return@setOnClickListener
            }

            // ================= FIREBASE AUTH =================
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        val uid = auth.currentUser!!.uid

                        val userMap = mapOf(
                            "email" to email,
                            "role" to "user"
                        )

                        // ================= SIMPAN KE DATABASE =================
                        database.reference.child("users")
                            .child(uid)
                            .setValue(userMap)
                            .addOnSuccessListener {

                                Toast.makeText(
                                    this,
                                    "Pendaftaran berhasil, silakan login",
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
                                    "Gagal menyimpan data ke database",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                    } else {
                        // ================= ERROR AUTH =================
                        val errorMsg = task.exception?.localizedMessage ?: "Registrasi gagal"
                        Toast.makeText(
                            this,
                            errorMsg,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        // ================= KE LOGIN =================
        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
