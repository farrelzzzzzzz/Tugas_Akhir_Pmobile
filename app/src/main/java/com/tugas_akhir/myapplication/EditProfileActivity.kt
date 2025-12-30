package com.tugas_akhir.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import id.zelory.compressor.Compressor
import java.io.File

class EditProfileActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var btnSave: Button
    private lateinit var progressUpload: ProgressBar

    private var imageUri: Uri? = null
    private val PICK_IMAGE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile_main)

        imgProfile = findViewById(R.id.imgProfile)
        btnSave = findViewById(R.id.btnSave)
        progressUpload = findViewById(R.id.progressUpload)

        findViewById<ImageView>(R.id.btnEditPhoto).setOnClickListener {
            pickImage()
        }

        btnSave.setOnClickListener {
            saveProfile()
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

    private fun saveProfile() {
        progressUpload.visibility = View.VISIBLE
        btnSave.isEnabled = false

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)

        uploadImageIfNeeded(dbRef)
    }

    private fun uploadImageIfNeeded(dbRef: DatabaseReference) {
        if (imageUri == null) {
            progressUpload.visibility = View.GONE
            finish()
            return
        }

        Thread {
            try {
                // Ambil file sementara dari Uri
                val inputStream = contentResolver.openInputStream(imageUri!!)
                val tempFile = File(cacheDir, "temp.jpg")
                inputStream?.use { file -> tempFile.outputStream().use { file.copyTo(it) } }

                // Kompres gambar
                val compressedFile = Compressor.compress(this, tempFile) {
                    defaultConfig {
                        quality = 60
                        format(Bitmap.CompressFormat.JPEG)
                    }
                }

                runOnUiThread {
                    // Upload ke Cloudinary
                    MediaManager.get().upload(compressedFile)
                        .param("folder", "profile_images")
                        .callback(object : UploadCallback {
                            override fun onSuccess(
                                requestId: String?,
                                resultData: Map<*, *>?
                            ) {
                                val url = resultData?.get("secure_url").toString()
                                dbRef.child("photoUrl").setValue(url)

                                progressUpload.visibility = View.GONE
                                btnSave.isEnabled = true
                                Toast.makeText(
                                    this@EditProfileActivity,
                                    "Profil berhasil disimpan",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }

                            override fun onError(requestId: String?, error: ErrorInfo?) {
                                progressUpload.visibility = View.GONE
                                btnSave.isEnabled = true
                                Toast.makeText(
                                    this@EditProfileActivity,
                                    "Upload gagal: ${error?.description}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                            override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                            override fun onStart(requestId: String?) {}
                        })
                        .dispatch()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressUpload.visibility = View.GONE
                    btnSave.isEnabled = true
                    Toast.makeText(
                        this@EditProfileActivity,
                        "Gagal memproses gambar: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }
}
