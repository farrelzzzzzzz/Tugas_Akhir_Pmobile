package com.tugas_akhir.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.text.SimpleDateFormat
import java.util.*

class PostHorizontalAdapter(
    private val postList: List<Post>
) : RecyclerView.Adapter<PostHorizontalAdapter.PostViewHolder>() {

    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPost: ImageView = view.findViewById(R.id.imgPost)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post_horizontal, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]

        // Load gambar dengan Glide
        Glide.with(holder.itemView.context)
            .load(post.imageUrl)
            .transform(RoundedCorners(24)) // Radius 24dp
            .into(holder.imgPost)

        // Tampilkan tanggal
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.tvDate.text =
            if (post.timestamp == 0L) "-"
            else sdf.format(Date(post.timestamp))
    }

    override fun getItemCount() = postList.size


}
