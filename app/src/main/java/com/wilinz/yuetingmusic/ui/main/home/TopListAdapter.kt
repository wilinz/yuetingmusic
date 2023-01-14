package com.wilinz.yuetingmusic.ui.main.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.wilinz.yuetingmusic.data.model.TopList.ListBean
import com.wilinz.yuetingmusic.data.model.TopListSong.PlaylistBean.TracksBean
import com.wilinz.yuetingmusic.databinding.ItemTopListBinding

class TopListAdapter(private var songs: MutableList<ListBean>) :
    RecyclerView.Adapter<TopListAdapter.MyViewHolder>() {
    private var listener: ((index0: Int, index1: Int, songs: List<TracksBean>, song: TracksBean) -> Unit)? =
        null

    fun setOnGetTracksListener(onGettracksListener: ((index: Int) -> Unit)?) {
        this.onGettracksListener = onGettracksListener
    }

    private var onGettracksListener: ((index: Int) -> Unit)? = null
    fun setOnRefresh(onRefresh: ((layout: SwipeRefreshLayout) -> Unit)?) {
        this.onRefresh = onRefresh
    }

    private var onRefresh: ((layout: SwipeRefreshLayout) -> Unit)? = null

    fun setItem(listBean: ListBean, index: Int) {
        songs[index] = listBean
        notifyItemChanged(index)
    }

    fun setItemTracks(tracks: List<TracksBean>, index: Int) {
        songs[index].tracks = tracks
        notifyItemChanged(index)
    }

    fun set(songs: MutableList<ListBean>) {
        this.songs = songs
        notifyDataSetChanged()
    }

    fun setOnSongClickListener(l: (index0: Int, index1: Int, songs: List<TracksBean>, song: TracksBean) -> Unit) {
        listener = l
    }

    class MyViewHolder(var binding: ItemTopListBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemTopListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = MyViewHolder(binding)
        binding.firstThreeMusic.layoutManager = LinearLayoutManager(parent.context)
        val adapter = TopListPlayListAdapter(java.util.List.of())
        adapter.setOnItemClickListener { songs1, index, song ->
            if (listener != null) {
                listener?.invoke(holder.absoluteAdapterPosition, index, songs1, song)
            }
        }
        binding.firstThreeMusic.adapter = adapter
        binding.root.setOnClickListener { v: View? -> }
        binding.swipeRefresh.setOnRefreshListener {
            if (onRefresh != null) {
                onRefresh?.invoke(binding.swipeRefresh)
            }
        }
        return holder
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val binding = holder.binding
        val top = songs[position]
        binding.topListName.text = top.name + " > "
        if (top.tracks == null) {
            onGettracksListener?.invoke(position)
        } else {
            val adapter = binding.firstThreeMusic.adapter as TopListPlayListAdapter?
            adapter!!.set(top.tracks!!)
        }
    }

    override fun getItemCount(): Int {
        return songs.size
    }
}