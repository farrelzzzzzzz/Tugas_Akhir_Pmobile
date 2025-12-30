package com.tugas_akhir.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvUsername: TextView
    private lateinit var tvBio: TextView
    private lateinit var imgProfile: ImageView

    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_main)

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        if (uid == null) {
            finish()
            return
        }

        dbRef = FirebaseDatabase.getInstance()
            .reference.child("users").child(uid)

        tvUsername = findViewById(R.id.tvUsername)
        tvBio = findViewById(R.id.tvBio)
        imgProfile = findViewById(R.id.imgProfile)

        findViewById<LinearLayout>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        loadProfile()
    }

    private fun loadProfile() {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                tvUsername.text = snapshot.child("username")
                    .getValue(String::class.java) ?: ""

                tvBio.text = snapshot.child("bio")
                    .getValue(String::class.java) ?: ""

                val photoUrl =
                    snapshot.child("photo").getValue(String::class.java)
                        ?: snapshot.child("photoUrl").getValue(String::class.java)

                if (!photoUrl.isNullOrEmpty()) {
                    Glide.with(this@ProfileActivity)
                        .load(photoUrl)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(
                            com.bumptech.glide.load.engine.DiskCacheStrategy.NONE
                        )
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(imgProfile)
                } else {
                    imgProfile.setImageResource(R.drawable.default_profile)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

}
