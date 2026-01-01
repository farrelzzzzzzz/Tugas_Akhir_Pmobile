package com.tugas_akhir.myapplication

import HorizontalSpaceItemDecoration
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.media.MediaScannerConnection
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class MenuActivity : AppCompatActivity() {

    // ===== CAMERA =====
    private lateinit var previewCamera: PreviewView
    private var imageCapture: ImageCapture? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private lateinit var cameraExecutor: ExecutorService

    // ===== BUTTON =====
    private lateinit var btnProfile: ImageView
    private lateinit var btnChat: ImageView
    private lateinit var btnGallery: ImageView
    private lateinit var btnSwitch: ImageView
    private lateinit var btnShutter: View
    private lateinit var btnEveryone: TextView

    // ===== FRIEND =====
    private lateinit var rvUsers: RecyclerView
    private lateinit var layoutFriends: View
    private lateinit var tvEmptyFriend: TextView
    private val userList = mutableListOf<User>()
    private lateinit var userAdapter: AdapterRecyclerView

    // ===== POST =====
    private val postList = mutableListOf<Post>()
    private lateinit var postAdapter: PostHorizontalAdapter

    private lateinit var viewPager: ViewPager2

    companion object {
        private const val GALLERY_REQUEST_CODE = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_main)

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        viewPager = findViewById(R.id.viewPagerPosts)
        setupViewPager(viewPager)

        loadUsers()
        loadAllPosts()
    }

    // ===== CAMERA =====
    private fun hasCameraPermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewCamera.surfaceProvider)

            imageCapture = ImageCapture.Builder().build()

            val selector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            provider.unbindAll()
            provider.bindToLifecycle(this, selector, preview, imageCapture)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val capture = imageCapture ?: return
        val file = File(
            externalMediaDirs.firstOrNull() ?: filesDir,
            "photo_${System.currentTimeMillis()}.jpg"
        )

        capture.takePicture(
            ImageCapture.OutputFileOptions.Builder(file).build(),
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    MediaScannerConnection.scanFile(
                        this@MenuActivity,
                        arrayOf(file.absolutePath),
                        arrayOf("image/jpeg"),
                        null
                    )

                    runOnUiThread {
                        startActivity(
                            Intent(this@MenuActivity, PreviewActivity::class.java)
                                .putExtra("image_uri", Uri.fromFile(file).toString())
                        )
                    }
                }

                override fun onError(exception: ImageCaptureException) {}
            }
        )
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)
        if (req == GALLERY_REQUEST_CODE && res == Activity.RESULT_OK) {
            data?.data?.let {
                startActivity(
                    Intent(this, PreviewActivity::class.java)
                        .putExtra("image_uri", it.toString())
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    // ===== USERS =====
    private fun loadUsers() {
        FirebaseDatabase.getInstance().getReference("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userList.clear()
                    snapshot.children.forEach {
                        val user = it.getValue(User::class.java)
                        if (user != null) userList.add(user.copy(uid = it.key ?: ""))
                    }
                    if (::userAdapter.isInitialized) userAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ===== POSTS =====
    private fun loadAllPosts() {
        FirebaseDatabase.getInstance().getReference("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postList.clear()
                    for (userSnap in snapshot.children) {
                        val uid = userSnap.key ?: continue
                        val postsSnap = userSnap.child("posts")
                        for (postSnap in postsSnap.children) {
                            val imageUrl = postSnap.child("imageUrl").getValue(String::class.java)
                            val timestamp = postSnap.child("timestamp").getValue(Long::class.java) ?: 0L
                            if (!imageUrl.isNullOrEmpty()) {
                                postList.add(Post(imageUrl, uid, timestamp, postSnap.key ?: ""))
                            }
                        }
                    }
                    postList.sortByDescending { it.timestamp }
                    if (::postAdapter.isInitialized) postAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // ===== VIEWPAGER2 =====
    private fun setupViewPager(viewPager: ViewPager2) {
        viewPager.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            override fun getItemCount() = 2
            override fun getItemViewType(position: Int) = position

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                return if (viewType == 0)
                    CameraHolder(layoutInflater.inflate(R.layout.page_camera, parent, false))
                else
                    PostHolder(layoutInflater.inflate(R.layout.page_post_feed, parent, false))
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                if (holder is PostHolder) holder.bind(postList)
            }

            // ===== CAMERA PAGE =====
            inner class CameraHolder(v: View) : RecyclerView.ViewHolder(v) {
                init {
                    previewCamera = v.findViewById(R.id.previewCamera)
                    btnProfile = v.findViewById(R.id.btnProfile)
                    btnChat = v.findViewById(R.id.btnChat)
                    btnGallery = v.findViewById(R.id.btnGallery)
                    btnSwitch = v.findViewById(R.id.btnSwitch)
                    btnShutter = v.findViewById(R.id.btnShutter)
                    btnEveryone = v.findViewById(R.id.btnEveryone)
                    rvUsers = v.findViewById(R.id.rvUsers)
                    layoutFriends = v.findViewById(R.id.layoutFriends)
                    tvEmptyFriend = v.findViewById(R.id.tvEmptyFriend)

                    rvUsers.layoutManager = LinearLayoutManager(this@MenuActivity)
                    userAdapter = AdapterRecyclerView(userList)
                    rvUsers.adapter = userAdapter

                    btnGallery.setOnClickListener {
                        val i = Intent(Intent.ACTION_PICK)
                        i.type = "image/*"
                        startActivityForResult(i, GALLERY_REQUEST_CODE)
                    }

                    btnSwitch.setOnClickListener {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                            CameraSelector.LENS_FACING_FRONT
                        else
                            CameraSelector.LENS_FACING_BACK
                        startCamera()
                    }

                    btnShutter.setOnClickListener { takePhoto() }

                    btnEveryone.setOnClickListener {
                        // Toggle visibilitas layoutFriends dengan daftar teman
                        if (layoutFriends.visibility == View.GONE) {
                            layoutFriends.visibility = View.VISIBLE
                            if (userList.isEmpty()) {
                                tvEmptyFriend.visibility = View.VISIBLE
                                rvUsers.visibility = View.GONE
                            } else {
                                tvEmptyFriend.visibility = View.GONE
                                rvUsers.visibility = View.VISIBLE
                            }
                        } else {
                            layoutFriends.visibility = View.GONE
                        }
                    }

                    btnProfile.setOnClickListener {
                        startActivity(Intent(this@MenuActivity, ProfileActivity::class.java))
                    }

                    if (hasCameraPermission()) startCamera()
                }
            }

            // ===== POST PAGE =====
            inner class PostHolder(v: View) : RecyclerView.ViewHolder(v) {
                private val rvPost: RecyclerView = v.findViewById(R.id.rvPostHorizontal)
                private val overlay: FrameLayout = v.findViewById(R.id.overlayLayout)

                private val btnProfile: ImageView = v.findViewById(R.id.btnProfile)
                private val btnEveryone: TextView = v.findViewById(R.id.btnEveryone)

                // Layout teman khusus PostHolder
                private val layoutFriendsPost: View = v.findViewById(R.id.layoutFriendsPost)
                private val rvUsersPost: RecyclerView = v.findViewById(R.id.rvUsers)
                private val tvEmptyFriendPost: TextView = v.findViewById(R.id.tvEmptyFriend)

                fun bind(posts: List<Post>) {
                    if (!::postAdapter.isInitialized) {
                        postAdapter = PostHorizontalAdapter(posts)
                        rvPost.layoutManager = LinearLayoutManager(this@MenuActivity, LinearLayoutManager.HORIZONTAL, false)
                        rvPost.adapter = postAdapter

                        val snapHelper = PagerSnapHelper()
                        snapHelper.attachToRecyclerView(rvPost)

                        rvPost.post {
                            val screenWidth = rvPost.width
                            val itemWidth = resources.getDimensionPixelSize(R.dimen.post_card_width)
                            val sidePadding = (screenWidth - itemWidth) / 2
                            rvPost.setPadding(sidePadding, 0, sidePadding, 0)
                        }
                        rvPost.clipToPadding = false
                    }

                    // RecyclerView teman di Post
                    rvUsersPost.layoutManager = LinearLayoutManager(this@MenuActivity)
                    val adapterPost = AdapterRecyclerView(userList)
                    rvUsersPost.adapter = adapterPost

                    // Touch handling overlay
                    rvPost.setOnTouchListener { view, event ->
                        val overlayLocation = IntArray(2)
                        overlay.getLocationOnScreen(overlayLocation)
                        val x = event.rawX.toInt()
                        val y = event.rawY.toInt()
                        val rect = android.graphics.Rect(
                            overlayLocation[0],
                            overlayLocation[1],
                            overlayLocation[0] + overlay.width,
                            overlayLocation[1] + overlay.height
                        )
                        if (rect.contains(x, y)) {
                            false
                        } else {
                            view.onTouchEvent(event)
                        }
                    }

                    // Tombol Everyone
                    btnEveryone.setOnClickListener {
                        if (layoutFriendsPost.visibility == View.GONE) {
                            layoutFriendsPost.visibility = View.VISIBLE
                            if (userList.isEmpty()) {
                                tvEmptyFriendPost.visibility = View.VISIBLE
                                rvUsersPost.visibility = View.GONE
                            } else {
                                tvEmptyFriendPost.visibility = View.GONE
                                rvUsersPost.visibility = View.VISIBLE
                            }
                        } else {
                            layoutFriendsPost.visibility = View.GONE
                        }
                    }

                    // Tombol Profile
                    btnProfile.setOnClickListener {
                        startActivity(Intent(this@MenuActivity, ProfileActivity::class.java))
                    }
                }
            }

        }

        viewPager.offscreenPageLimit = 2
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == 0 && hasCameraPermission()) startCamera()
            }
        })
    }

    // ===== POST HORIZONTAL ADAPTER =====
    inner class PostHorizontalAdapter(private val posts: List<Post>) :
        RecyclerView.Adapter<PostHorizontalAdapter.PostViewHolder>() {

        inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imgPost: ImageView = view.findViewById(R.id.imgPost)
            val tvDate: TextView = view.findViewById(R.id.tvDate)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
            val view = layoutInflater.inflate(R.layout.item_post_horizontal, parent, false)
            return PostViewHolder(view)
        }

        override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
            val post = posts[position]
            Glide.with(holder.itemView.context)
                .load(post.imageUrl)
                .transform(RoundedCorners(24))
                .into(holder.imgPost)

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            holder.tvDate.text = if (post.timestamp == 0L) "-" else sdf.format(Date(post.timestamp))
        }

        override fun getItemCount() = posts.size
    }
}
