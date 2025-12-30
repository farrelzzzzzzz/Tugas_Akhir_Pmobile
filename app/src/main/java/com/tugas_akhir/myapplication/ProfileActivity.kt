package com.tugas_akhir.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var tvUsername: TextView
    private lateinit var tvBio: TextView
    private lateinit var imgProfile: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_main)

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return

        dbRef = FirebaseDatabase.getInstance()
            .reference.child("users").child(uid)

        tvUsername = findViewById(R.id.tvUsername)
        tvBio = findViewById(R.id.tvBio)
        imgProfile = findViewById(R.id.imgProfile)

        val btnEditProfile = findViewById<LinearLayout>(R.id.btnEditProfile)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        loadProfile()
    }

    private fun loadProfile() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                tvUsername.text = snapshot.child("username")
                    .getValue(String::class.java) ?: ""

                tvBio.text = snapshot.child("bio")
                    .getValue(String::class.java) ?: ""

                val photoUrl = snapshot.child("photoUrl")
                    .getValue(String::class.java)

                if (!photoUrl.isNullOrEmpty()) {
                    Glide.with(this@ProfileActivity)
                        .load(photoUrl)
                        .placeholder(R.drawable.default_profile)
                        .into(imgProfile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ProfileActivity,
                    "Gagal memuat profil",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
