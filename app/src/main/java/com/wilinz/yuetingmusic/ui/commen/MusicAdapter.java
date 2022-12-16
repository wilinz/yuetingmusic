package com.wilinz.yuetingmusic.ui.commen;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.databinding.ItemMusicBinding;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    private List<Song> songs;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(List<Song> songs, int index, Song song);
    }

    public MusicAdapter(@NonNull List<Song> musics) {
        this.songs = musics;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        listener = l;
    }

    class MusicViewHolder extends RecyclerView.ViewHolder {

        ItemMusicBinding binding;

        public MusicViewHolder(@NonNull ItemMusicBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMusicBinding binding = ItemMusicBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        MusicViewHolder holder = new MusicViewHolder(binding);
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
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        ItemMusicBinding binding = holder.binding;
        Song song = songs.get(position);
        binding.name.setText(song.title);
        binding.serialNumber.setText((position + 1) + "");
        binding.secondName.setText(song.artist + "-" + song.album);
        Glide.with(binding.songAvatar)
                .load(song.coverImgUrl)
                .error(R.drawable.icon)
                .into(binding.songAvatar);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }


}
