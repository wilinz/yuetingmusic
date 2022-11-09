package com.wilinz.yuetingmusic.ui.main.home;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.databinding.ItemMusicBinding;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    private List<Song> musics;
    private OnItemClickListener listener;

    interface OnItemClickListener {
        void onItemClick(int index, Song song);
    }

    public MusicAdapter(@NonNull List<Song> musics) {
        this.musics = musics;
    }

    public void setMusics(List<Song> musics) {
        this.musics = musics;
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
                int index = holder.getAdapterPosition();
                listener.onItemClick(index, musics.get(index));
            }
        });
        return holder;
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
