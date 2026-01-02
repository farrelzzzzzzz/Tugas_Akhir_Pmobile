package com.tugas_akhir.myapplication

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AdapterRecyclerView(
    private val userList: List<User>,
    private val onClick: (User) -> Unit
) : RecyclerView.Adapter<AdapterRecyclerView.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imgUser: ImageView = itemView.findViewById(R.id.imgUser)
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)

        fun bind(user: User) {
            tvUsername.text = user.username

            Glide.with(itemView.context)
                .load(user.photoUrl)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(imgUser)

            itemView.setOnClickListener {
                onClick(user)
            }

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ProfileActivity::class.java)
                intent.putExtra("USER_ID", user.uid)
                itemView.context.startActivity(intent)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int = userList.size


}
