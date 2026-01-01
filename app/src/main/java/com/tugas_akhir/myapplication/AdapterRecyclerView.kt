package com.tugas_akhir.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
//list user menu utama
class AdapterRecyclerView(
    private val userList: List<User>
) : RecyclerView.Adapter<AdapterRecyclerView.UserViewHolder>() {

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvUsername)
        val imgUser: ImageView = view.findViewById(R.id.imgUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.tvName.text = user.username
        Glide.with(holder.itemView.context)
            .load(user.photoUrl)
            .placeholder(R.drawable.default_profile)
            .into(holder.imgUser)
    }

    override fun getItemCount(): Int = userList.size
}
