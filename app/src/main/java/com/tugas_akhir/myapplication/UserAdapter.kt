package com.tugas_akhir.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class UserAdapter(
    private val userList: List<User>
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        // NULL-SAFE (ANTI CRASH)
        val tvName: TextView? = view.findViewById(R.id.tvUsername)
        val imgUser: ImageView? = view.findViewById(R.id.imgUser)


    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        // SET NAMA USER (AMAN)
        holder.tvName?.text = user.username

        // LOAD FOTO USER (AMAN)
        holder.imgUser?.let { imageView ->
            Glide.with(imageView.context)
                .load(user.photoUrl)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(imageView)
        }


    }

    override fun getItemCount(): Int = userList.size
}
