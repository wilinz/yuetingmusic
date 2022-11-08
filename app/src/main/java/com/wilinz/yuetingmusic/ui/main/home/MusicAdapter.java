package com.wilinz.yuetingmusic.ui.main.home;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.databinding.ItemMusicBinding;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    private List<Song> musics;

    public MusicAdapter(@NonNull List<Song> musics) {
        this.musics = musics;
    }

    public void setMusics(List<Song> musics){
        this.musics=musics;
        notifyDataSetChanged();
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
        return new MusicViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        ItemMusicBinding binding = holder.binding;
        Song music = musics.get(position);
        binding.name.setText(music.song);
        binding.desc.setText(music.singer + "-" + music.album);
    }

    @Override
    public int getItemCount() {
        return musics.size();
    }


}
