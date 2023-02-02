package com.wilinz.yuetingmusic.ui.user

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wilinz.yuetingmusic.R
import com.wilinz.yuetingmusic.data.model.User
import com.wilinz.yuetingmusic.databinding.ItemUserBinding
import com.wilinz.yuetingmusic.ui.user.UserListAdapter.UserListViewHolder
import com.wilinz.yuetingmusic.util.ScreenUtil

class UserListAdapter(private var users: List<User>) : RecyclerView.Adapter<UserListViewHolder>() {
    private var listener: ((users: List<User>, index: Int, user: User)->Unit)? = null

    interface OnItemClickListener {
        fun onItemClick(users: List<User>?, index: Int, user: User?)
    }

    fun setUsers(users: List<User>) {
        this.users = users
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(l: (users: List<User>, index: Int, user: User)->Unit) {
        listener = l
    }

    class UserListViewHolder(var binding: ItemUserBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = UserListViewHolder(binding)
        binding.root.setOnClickListener { v: View? ->
            val currentUserIndex = users.indexOfFirst { user: User -> user.isActive }
            val index = holder.absoluteAdapterPosition
            if (index != currentUserIndex && listener != null) {
                listener?.invoke(users, index, users[index])
            }
        }
        return holder
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
        val binding = holder.binding
        val user = users[position]
        binding.name.text = user.username
        //        binding.serialNumber.setText((position + 1) + "");
        binding.secondName.text = user.nickname
        if (user.isActive) {
            binding.tag.text = "当前用户"
        } else {
            binding.tag.text = ""
        }
        val size = ScreenUtil.dpToPx(binding.songAvatar.context, 48)
        Glide.with(binding.songAvatar)
            .load(user.avatar + "?param=${size}y${size}")
            .error(R.drawable.avatar2)
            .into(binding.songAvatar)
    }

    override fun getItemCount(): Int {
        return users.size
    }
}