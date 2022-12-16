package com.wilinz.yuetingmusic.ui.playlist;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.databinding.FragmentPlaylistBinding;
import com.wilinz.yuetingmusic.ui.commen.MusicAdapter;

import java.util.List;

public class PlaylistFragment extends Fragment {

    private PlaylistViewModel viewModel;
    public FragmentPlaylistBinding binding;
    private List<Song> songs;

    public PlaylistFragment(){}

    public PlaylistFragment(List<Song> songs) {
        this.songs = songs;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPlaylistBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PlaylistViewModel.class);
        MusicAdapter adapter = new MusicAdapter(List.of());
        adapter.setOnItemClickListener((songs, index, song) -> {
            viewModel.playFromUri(songs, song);
        });
        binding.playList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.playList.setAdapter(adapter);
        binding.refresh.setColorSchemeResources(R.color.my_primary);
    }

}