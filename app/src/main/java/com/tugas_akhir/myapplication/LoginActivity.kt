package com.tugas_akhir.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_main)

        auth = FirebaseAuth.getInstance()

        val edUsername = findViewById<EditText>(R.id.edUsername)
        val edPassword = findViewById<EditText>(R.id.edPassword)
        val btnLogin = findViewById<AppCompatButton>(R.id.btnLogin)
        val btnSignIn = findViewById<AppCompatButton>(R.id.btnSignIn)

       
        val passwordHint = "Masukkan kata sandi anda"
        edPassword.hint = passwordHint

        edPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                edPassword.hint = if (s.isNullOrEmpty()) passwordHint else null
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        
        btnLogin.setOnClickListener {
            val email = edUsername.text.toString().trim()
            val password = edPassword.text.toString().trim()

            if (email.isEmpty()) {
                edUsername.error = "Email tidak boleh kosong"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                edPassword.error = "Password tidak boleh kosong"
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        Toast.makeText(
                            this,
                            "Login berhasil",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(
                            Intent(this, MenuActivity::class.java)
                        )
                        finish()

                    } else {
                        Toast.makeText(
                            this,
                            task.exception?.message ?: "Login gagal",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        btnSignIn.setOnClickListener {
            startActivity(Intent(this, SignActivity::class.java))
        }
    }
}
