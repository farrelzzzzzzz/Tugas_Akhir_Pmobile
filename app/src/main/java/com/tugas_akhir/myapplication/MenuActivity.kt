package com.tugas_akhir.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.media.MediaScannerConnection
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MenuActivity : AppCompatActivity() {

    private lateinit var previewCamera: PreviewView
    private lateinit var btnProfile: ImageView
    private lateinit var btnChat: ImageView
    private lateinit var btnGallery: ImageView
    private lateinit var btnSwitch: ImageView
    private lateinit var btnShutter: View
    private lateinit var btnEveryone: TextView
    private lateinit var rvUsers: RecyclerView
    private lateinit var layoutFriends: View
    private lateinit var tvEmptyFriend: TextView

    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private val userList = mutableListOf<User>()
    private lateinit var userAdapter: AdapterRecyclerView

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100
        private const val GALLERY_REQUEST_CODE = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_main)

        // CEK USER LOGIN
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // INIT VIEW
        previewCamera = findViewById(R.id.previewCamera)
        btnProfile = findViewById(R.id.btnProfile)
        btnChat = findViewById(R.id.btnChat)
        btnGallery = findViewById(R.id.btnGallery)
        btnSwitch = findViewById(R.id.btnSwitch)
        btnShutter = findViewById(R.id.btnShutter)
        btnEveryone = findViewById(R.id.btnEveryone)
        rvUsers = findViewById(R.id.rvUsers)
        layoutFriends = findViewById(R.id.layoutFriends)
        tvEmptyFriend = findViewById(R.id.tvEmptyFriend)

        // Setup RecyclerView untuk list user
        rvUsers.layoutManager = LinearLayoutManager(this)
        userAdapter = AdapterRecyclerView(userList)
        rvUsers.adapter = userAdapter

        rvUsers.visibility = View.GONE
        layoutFriends.visibility = View.GONE

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (hasCameraPermission()) startCamera() else requestCameraPermission()

        // LISTENER BUTTON
        btnProfile.setOnClickListener {
            val intent = Intent(this@MenuActivity, ProfileActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        btnChat.setOnClickListener { Toast.makeText(this, "Chat clicked", Toast.LENGTH_SHORT).show() }

        btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }

        btnSwitch.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
            startCamera()
        }

        btnShutter.setOnClickListener { takePhoto() }

        // Toggle tampilkan list user di kotak border
        btnEveryone.setOnClickListener {
            if (userList.isNotEmpty()) {
                if (layoutFriends.visibility == View.GONE) {
                    layoutFriends.visibility = View.VISIBLE
                    rvUsers.visibility = View.VISIBLE
                    tvEmptyFriend.visibility = View.GONE
                } else {
                    layoutFriends.visibility = View.GONE
                    rvUsers.visibility = View.GONE
                }
            } else {
                layoutFriends.visibility = View.VISIBLE
                rvUsers.visibility = View.GONE
                tvEmptyFriend.visibility = View.VISIBLE
            }
        }

        loadUsersFromFirebase()
    }

    private fun loadUsersFromFirebase() {
        val dbRef = FirebaseDatabase.getInstance().getReference("users")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (data in snapshot.children) {
                    val user = data.getValue(User::class.java)
                    if (user != null) userList.add(user)
                }
                Log.d("MenuActivity", "userList size: ${userList.size}")
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MenuActivity, "Gagal load user", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun hasCameraPermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission diperlukan", Toast.LENGTH_LONG).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewCamera.surfaceProvider) }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Gagal menyalakan kamera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(externalMediaDirs.firstOrNull() ?: filesDir, "photo_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                MediaScannerConnection.scanFile(this@MenuActivity, arrayOf(photoFile.absolutePath), arrayOf("image/jpeg")) { _, _ ->
                    runOnUiThread {
                        Toast.makeText(this@MenuActivity, "Foto tersimpan di galeri", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@MenuActivity, PreviewActivity::class.java)
                        intent.putExtra("photo_path", photoFile.absolutePath)
                        startActivity(intent)
                    }
                }
            }

            override fun onError(exception: ImageCaptureException) {
                runOnUiThread {
                    Toast.makeText(this@MenuActivity, "Gagal mengambil foto", Toast.LENGTH_SHORT).show()
                }
                exception.printStackTrace()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                Toast.makeText(this, "Foto dipilih: $selectedImageUri", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
