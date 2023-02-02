package com.wilinz.yuetingmusic.ui.main.home

import android.annotation.SuppressLint
import android.app.Application
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wilinz.yuetingmusic.data.model.TopListSong.PlaylistBean.TracksBean
import com.wilinz.yuetingmusic.databinding.ItemMusicBinding
import com.wilinz.yuetingmusic.util.ScreenUtil

class TopListPlayListAdapter(private var songs: List<TracksBean>) :
    RecyclerView.Adapter<TopListPlayListAdapter.MyViewHolder>() {
    private var listener: ((songs: List<TracksBean>, index: Int, song: TracksBean) -> Unit)? = null

    interface OnItemClickListener {
        fun onItemClick(songs: List<TracksBean>?, index: Int, song: TracksBean?)
    }

    fun set(songs: List<TracksBean>) {
        this.songs = songs
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(l: (songs: List<TracksBean>, index: Int, song: TracksBean) -> Unit) {
        listener = l
    }

    class MyViewHolder(var binding: ItemMusicBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = MyViewHolder(binding)
        binding.root.setOnClickListener { v: View? ->
            if (listener != null) {
                val index = holder.absoluteAdapterPosition
                listener?.invoke(songs, index, songs[index])
            }
        }
        return holder
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val binding = holder.binding
        val track = songs[position]
        binding.serialNumber.text = (position + 1).toString() + ""
        binding.name.text = track.name
        val size = ScreenUtil.dpToPx(binding.songAvatar.context, 48)
        Glide.with(binding.songAvatar)
            .load(track.al!!.picUrl + "?param=${size}y${size}")
            .into(binding.songAvatar)
        val artist = track.ar!!.firstOrNull()
        if (artist != null) binding.secondName.text = artist.name
    }

    override fun getItemCount(): Int {
        return songs.size
    }
}