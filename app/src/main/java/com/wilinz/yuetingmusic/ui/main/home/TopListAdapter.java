package com.wilinz.yuetingmusic.ui.main.home;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.wilinz.yuetingmusic.data.model.TopList;
import com.wilinz.yuetingmusic.data.model.TopListSong;
import com.wilinz.yuetingmusic.databinding.ItemTopListBinding;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public class TopListAdapter extends RecyclerView.Adapter<TopListAdapter.MyViewHolder> {

    private List<TopList.ListBean> songs;
    private OnItemClickListener listener;

    public void setOnGetTracksListener(OnGetTracksListener onGettracksListener) {
        this.onGettracksListener = onGettracksListener;
    }

    private OnGetTracksListener onGettracksListener;

    public void setOnRefresh(OnRefresh onRefresh) {
        this.onRefresh = onRefresh;
    }

    interface OnItemClickListener {
        void onItemClick(int index0, int index1, List<TopListSong.PlaylistBean.TracksBean> songs, TopListSong.PlaylistBean.TracksBean song);
    }

    interface OnGetTracksListener {
        void get(int index);
    }

    private OnRefresh onRefresh;

    public interface OnRefresh {
        void run(SwipeRefreshLayout layout);
    }

    public void setItem(TopList.ListBean listBean, int index) {
        songs.set(index, listBean);
        notifyItemChanged(index);
    }

    public void setItemTracks(List<TopListSong.PlaylistBean.TracksBean> tracks, int index) {
        songs.get(index).tracks = tracks;
        notifyItemChanged(index);
    }

    public TopListAdapter(@NonNull List<TopList.ListBean> musics) {
        this.songs = musics;
    }

    public void set(List<TopList.ListBean> songs) {
        this.songs = songs;
        notifyDataSetChanged();
    }

    public void setOnSongClickListener(OnItemClickListener l) {
        listener = l;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        public ItemTopListBinding binding;

        public MyViewHolder(@NonNull ItemTopListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTopListBinding binding = ItemTopListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        MyViewHolder holder = new MyViewHolder(binding);
        binding.firstThreeMusic.setLayoutManager(new LinearLayoutManager(parent.getContext()));
        TopListPlayListAdapter adapter = new TopListPlayListAdapter(List.of());
        adapter.setOnItemClickListener(((songs1, index, song) -> {
            if (listener != null) {
                listener.onItemClick(holder.getAbsoluteAdapterPosition(), index, songs1, song);
            }
        }));
        binding.firstThreeMusic.setAdapter(adapter);
        binding.getRoot().setOnClickListener(v -> {
//            if (listener != null) {
//                int index = holder.getAbsoluteAdapterPosition();
//                listener.onItemClick(songs, index, songs.get(index));
//            }
        });
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (this.onRefresh != null) {
                onRefresh.run(binding.swipeRefresh);
            }
        });
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ItemTopListBinding binding = holder.binding;
        TopList.ListBean top = songs.get(position);
        binding.topListName.setText(top.name + " > ");
        if (top.tracks == null) {
            if (onGettracksListener != null) onGettracksListener.get(position);
        } else {
            TopListPlayListAdapter adapter = (TopListPlayListAdapter) binding.firstThreeMusic.getAdapter();
            adapter.set(top.tracks);
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }


}
