package com.tugas_akhir.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvUsername: TextView
    private lateinit var tvBio: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var btnBuatPostingan: Button

    private lateinit var rvPost: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val postList = ArrayList<Post>()

    private lateinit var auth: FirebaseAuth
    private lateinit var userRef: DatabaseReference
    private lateinit var postRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_main)

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return

        userRef = FirebaseDatabase.getInstance()
            .reference.child("users").child(uid)

        postRef = FirebaseDatabase.getInstance()
            .reference.child("users").child(uid).child("posts")

        tvUsername = findViewById(R.id.tvUsername)
        tvBio = findViewById(R.id.tvBio)
        imgProfile = findViewById(R.id.imgProfile)
        btnBack = findViewById(R.id.btnBack)
        btnBuatPostingan = findViewById(R.id.btnBuatPostingan)

        rvPost = findViewById(R.id.rvPost)
        rvPost.layoutManager = GridLayoutManager(this, 3)

        postAdapter = PostAdapter(postList) { post ->
            confirmDelete(post)
        }

        rvPost.adapter = postAdapter

        btnBack.setOnClickListener { finish() }

        btnBuatPostingan.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
        }

        loadProfile()
        loadPost()
    }

    private fun loadProfile() {
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tvUsername.text =
                    snapshot.child("username").getValue(String::class.java) ?: ""

                tvBio.text =
                    snapshot.child("bio").getValue(String::class.java) ?: ""

                val photoUrl =
                    snapshot.child("photoUrl").getValue(String::class.java)

                if (!photoUrl.isNullOrEmpty()) {
                    Glide.with(this@ProfileActivity)
                        .load(photoUrl)
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

    private fun loadPost() {
        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear()
                for (snap in snapshot.children) {
                    val post = snap.getValue(Post::class.java)
                    if (post != null) {
                        post.postId = snap.key ?: ""
                        postList.add(post)
                    }
                }
                postAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun confirmDelete(post: Post) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Postingan")
            .setMessage("Yakin ingin menghapus postingan ini?")
            .setPositiveButton("Hapus") { _, _ ->
                deletePost(post)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    /**
     * 🔥 DELETE YANG BENAR
     * ❌ JANGAN delete Cloudinary di Android
     * ✅ DELETE FIREBASE SAJA
     */
    private fun deletePost(post: Post) {
        postRef.child(post.postId)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Postingan berhasil dihapus",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Gagal menghapus postingan",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
