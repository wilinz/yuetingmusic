package com.wilinz.yuetingmusic.ui.commen

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wilinz.yuetingmusic.R
import com.wilinz.yuetingmusic.data.model.Song
import com.wilinz.yuetingmusic.databinding.ItemMusicBinding
import com.wilinz.yuetingmusic.ui.commen.MusicAdapter.MusicViewHolder
import com.wilinz.yuetingmusic.util.ScreenUtil

class MusicAdapter(private var songs: List<Song>) : RecyclerView.Adapter<MusicViewHolder>() {
    private var listener: ((songs: List<Song>, index: Int, song: Song) -> Unit)? = null


    interface OnItemClickListener {
        fun onItemClick(songs: List<Song>?, index: Int, song: Song?)
    }

    fun setSongs(songs: List<Song>) {
        this.songs = songs
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(l: (songs: List<Song>, index: Int, song: Song) -> Unit) {
        listener = l
    }

    inner class MusicViewHolder(var binding: ItemMusicBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val binding = ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = MusicViewHolder(binding)
        binding.root.setOnClickListener { v: View? ->
            listener?.invoke(
                songs,
                holder.absoluteAdapterPosition,
                songs[holder.absoluteAdapterPosition]
            )
        }
        return holder
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val binding = holder.binding
        val song = songs[position]
        binding.name.text = song.title
        binding.serialNumber.text = (position + 1).toString() + ""
        binding.secondName.text = song.artist + "-" + song.album
        val size = ScreenUtil.dpToPx(binding.songAvatar.context, 48)
        Glide.with(binding.songAvatar)
            .load(song.coverImgUrl + "?param=${size}y${size}")
            .error(R.drawable.icon)
            .into(binding.songAvatar)
    }

    override fun getItemCount(): Int {
        return songs.size
    }
}