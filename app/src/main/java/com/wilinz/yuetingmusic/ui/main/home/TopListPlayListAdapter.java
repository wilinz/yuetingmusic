package com.wilinz.yuetingmusic.ui.main.home;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.wilinz.yuetingmusic.data.model.TopList;
import com.wilinz.yuetingmusic.data.model.TopListSong;
import com.wilinz.yuetingmusic.databinding.ItemFirstThreeListBinding;
import com.wilinz.yuetingmusic.databinding.ItemTopListBinding;

import java.util.List;

import kotlin.collections.CollectionsKt;


public class TopListPlayListAdapter extends RecyclerView.Adapter<TopListPlayListAdapter.MyViewHolder> {

    private List<TopListSong.PlaylistBean.TracksBean> songs;
    private OnItemClickListener listener;

    interface OnItemClickListener {
        void onItemClick(List<TopListSong.PlaylistBean.TracksBean> songs, int index, TopListSong.PlaylistBean.TracksBean song);
    }

    public TopListPlayListAdapter(@NonNull List<TopListSong.PlaylistBean.TracksBean> musics) {
        this.songs = musics;
    }

    public void set(List<TopListSong.PlaylistBean.TracksBean> songs) {
        this.songs = songs;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        listener = l;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        ItemFirstThreeListBinding binding;

        public MyViewHolder(@NonNull ItemFirstThreeListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFirstThreeListBinding binding = ItemFirstThreeListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        MyViewHolder holder = new MyViewHolder(binding);

        binding.getRoot().setOnClickListener(v -> {
            if (listener != null) {
                int index = holder.getAbsoluteAdapterPosition();
                listener.onItemClick(songs, index, songs.get(index));
            }
        });
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TopListPlayListAdapter.MyViewHolder holder, int position) {
        ItemFirstThreeListBinding binding = holder.binding;
        TopListSong.PlaylistBean.TracksBean track = songs.get(position);
        binding.serialNumber.setText((position + 1) + "");
        binding.songName.setText(track.name);

        Glide.with(binding.songAvatar)
                .load(track.al.picUrl)
                .into(binding.songAvatar);

        TopListSong.PlaylistBean.TracksBean.ArBean artist = CollectionsKt.firstOrNull(track.ar);
        if (artist != null) binding.songArtist.setText(artist.name);
    }

    @Override
    public int getItemCount() {
        int size = songs.size();
        return size;
    }


}
