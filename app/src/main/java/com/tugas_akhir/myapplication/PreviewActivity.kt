package com.tugas_akhir.myapplication

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class PreviewActivity : AppCompatActivity() {

    private lateinit var imgPreview: ImageView
    private lateinit var btnUpload: Button

    private lateinit var btnCancelUpload: Button
    private lateinit var btnBack: ImageView
    private lateinit var progressOverlay: FrameLayout

    private var imageUri: Uri? = null

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_main)

        imgPreview = findViewById(R.id.imgPreview)
        btnUpload = findViewById(R.id.btnUpload)
        btnCancelUpload = findViewById(R.id.btnCancelUpload)
        btnBack = findViewById(R.id.btnBack)
        progressOverlay = findViewById(R.id.progressOverlay)

        imageUri = intent.getStringExtra("image_uri")?.let { Uri.parse(it) }
        imgPreview.setImageURI(imageUri)

        btnUpload.setOnClickListener { uploadImage() }

        // ===== BATAL UPLOAD =====
        btnCancelUpload.setOnClickListener {
            finish() // langsung kembali ke MenuActivity
        }

        // ===== BACK BUTTON =====
        btnBack.setOnClickListener {
            finish() // back ke MenuActivity juga
        }
    }

    private fun uploadImage() {
        val uid = auth.currentUser?.uid ?: return
        val uri = imageUri ?: return

        progressOverlay.visibility = View.VISIBLE

        MediaManager.get()
            .upload(uri)
            .option("folder", "users/$uid/posts")
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val imageUrl = resultData?.get("secure_url").toString()
                    savePost(uid, imageUrl)
                }

                override fun onError(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {
                    progressOverlay.visibility = View.GONE
                }

                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {}
            })
            .dispatch()
    }

    private fun savePost(uid: String, imageUrl: String) {
        val postId = db.child("posts").push().key ?: return

        val data = mapOf(
            "postId" to postId,
            "imageUrl" to imageUrl,
            "userId" to uid,
            "timestamp" to System.currentTimeMillis()
        )

        db.child("posts").child(postId).setValue(data)
        db.child("users").child(uid).child("posts").child(postId).setValue(data)
            .addOnSuccessListener {
                progressOverlay.visibility = View.GONE
                finish()
            }
    }
}
