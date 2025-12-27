package com.tugas_akhir.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.media.MediaScannerConnection
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
    private lateinit var layoutFriends: LinearLayout

    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100
        private const val GALLERY_REQUEST_CODE = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_main)

        // INIT VIEW
        previewCamera = findViewById(R.id.previewCamera)
        btnProfile = findViewById(R.id.btnProfile)
        btnChat = findViewById(R.id.btnChat)
        btnGallery = findViewById(R.id.btnGallery)
        btnSwitch = findViewById(R.id.btnSwitch)
        btnShutter = findViewById(R.id.btnShutter)
        btnEveryone = findViewById(R.id.btnEveryone)
        layoutFriends = findViewById(R.id.layoutFriends)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // CEK PERMISSION CAMERA
        if (hasCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }

        // CLICK LISTENER
        btnProfile.setOnClickListener {
            val intent = Intent(this@MenuActivity, ProfileActivity::class.java)
            startActivity(intent)
            overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
        }



        btnChat.setOnClickListener {
            Toast.makeText(this, "Chat clicked", Toast.LENGTH_SHORT).show()
        }

        // Tombol gallery buka galeri HP
        btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }

        // Tombol switch kamera
        btnSwitch.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                CameraSelector.LENS_FACING_FRONT
            else
                CameraSelector.LENS_FACING_BACK

            startCamera()
        }

        // Tombol shutter
        btnShutter.setOnClickListener {
            takePhoto()
        }

        btnEveryone.setOnClickListener {
            layoutFriends.visibility =
                if (layoutFriends.visibility == View.GONE) View.VISIBLE else View.GONE
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Camera permission diperlukan",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewCamera.surfaceProvider) }

            // ImageCapture
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            // CameraSelector
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Gagal menyalakan kamera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            externalMediaDirs.firstOrNull() ?: filesDir,
            "photo_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // Scan media supaya muncul di galeri
                    MediaScannerConnection.scanFile(
                        this@MenuActivity,
                        arrayOf(photoFile.absolutePath),
                        arrayOf("image/jpeg")
                    ) { path: String, uri: Uri ->
                        runOnUiThread {
                            Toast.makeText(
                                this@MenuActivity,
                                "Foto tersimpan di galeri",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@MenuActivity,
                            "Gagal mengambil foto",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    exception.printStackTrace()
                }
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                Toast.makeText(this, "Foto dipilih: $selectedImageUri", Toast.LENGTH_SHORT).show()
                // Bisa ditampilkan di ImageView atau diproses sesuai kebutuhan
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
