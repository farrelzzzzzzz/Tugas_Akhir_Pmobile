package com.tugas_akhir.myapplication

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
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

        intent.getStringExtra("image_uri")?.let {
            imageUri = Uri.parse(it)
            imgPreview.setImageURI(imageUri)
        }

        btnUpload.setOnClickListener { uploadPostImage() }
        btnCancelUpload.setOnClickListener { finish() }
        btnBack.setOnClickListener { finish() }
    }

    private fun uploadPostImage() {

        val uid = auth.currentUser?.uid
        if (uid == null || imageUri == null) {
            Toast.makeText(this, "User / gambar tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        progressOverlay.visibility = View.VISIBLE

        // 🔥 SAMA PERSIS DENGAN EDIT PROFILE
        MediaManager.get()
            .upload(imageUri)
            .option("folder", "users/$uid/posts")
            .callback(object : UploadCallback {

                override fun onStart(requestId: String?) {}

                override fun onProgress(
                    requestId: String?,
                    bytes: Long,
                    totalBytes: Long
                ) {}

                override fun onSuccess(
                    requestId: String?,
                    resultData: Map<*, *>?
                ) {
                    val imageUrl = resultData?.get("secure_url").toString()
                    val publicId = resultData?.get("public_id").toString()
                    savePost(uid, imageUrl, publicId)
                }

                override fun onError(
                    requestId: String?,
                    error: ErrorInfo?
                ) {
                    progressOverlay.visibility = View.GONE
                    Toast.makeText(
                        this@PreviewActivity,
                        error?.description ?: "Upload gagal",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }

    private fun savePost(uid: String, imageUrl: String, publicId: String) {

        val postRef = db.child("users").child(uid).child("posts")
        val postId = postRef.push().key ?: return

        val data = hashMapOf(
            "imageUrl" to imageUrl,
            "publicId" to publicId,
            "timestamp" to System.currentTimeMillis()
        )

        postRef.child(postId).setValue(data)
            .addOnSuccessListener {
                progressOverlay.visibility = View.GONE
                Toast.makeText(this, "Postingan berhasil diupload", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                progressOverlay.visibility = View.GONE
                Toast.makeText(this, "Gagal menyimpan postingan", Toast.LENGTH_SHORT).show()
            }
    }
}
