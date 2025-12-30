package com.tugas_akhir.myapplication

import android.app.Activity
import android.content.Intent
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

class EditProfileActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var btnEditPhoto: ImageView
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var progressOverlay: FrameLayout

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etBio: EditText

    private var imageUri: Uri? = null
    private val PICK_IMAGE = 101

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile_main)

        imgProfile = findViewById(R.id.imgProfile)
        btnEditPhoto = findViewById(R.id.btnEditPhoto)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        progressOverlay = findViewById(R.id.progressOverlay)

        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etBio = findViewById(R.id.etBio)

        btnEditPhoto.setOnClickListener { pickImage() }
        btnCancel.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            if (imageUri != null) {
                uploadImageToCloudinary()
            } else {
                saveProfile(null)
            }
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imgProfile.setImageURI(imageUri)
        }
    }

    private fun uploadImageToCloudinary() {
        progressOverlay.visibility = View.VISIBLE

        MediaManager.get()
            .upload(imageUri)
            .option("folder", "profile_images")
            .callback(object : UploadCallback {

                override fun onStart(requestId: String?) {}

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val imageUrl = resultData?.get("secure_url").toString()
                    saveProfile(imageUrl)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    progressOverlay.visibility = View.GONE
                    Toast.makeText(
                        this@EditProfileActivity,
                        error?.description ?: "Upload gagal",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }

    private fun saveProfile(photoUrl: String?) {
        val uid = auth.currentUser?.uid ?: return

        val userData = hashMapOf(
            "username" to etUsername.text.toString(),
            "email" to etEmail.text.toString(),
            "phone" to etPhone.text.toString(),
            "bio" to etBio.text.toString()
        )

        if (photoUrl != null) {
            userData["photoUrl"] = photoUrl
        }

        db.child("users").child(uid).updateChildren(userData as Map<String, Any>)
            .addOnSuccessListener {
                progressOverlay.visibility = View.GONE
                Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                progressOverlay.visibility = View.GONE
                Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
            }
    }
}
