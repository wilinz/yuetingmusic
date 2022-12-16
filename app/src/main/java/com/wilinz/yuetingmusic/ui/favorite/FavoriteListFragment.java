package com.wilinz.yuetingmusic.ui.favorite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.databinding.FragmentPlaylistContainerBinding;
import com.wilinz.yuetingmusic.event.FavoriteUpdatedEvent;
import com.wilinz.yuetingmusic.ui.commen.MusicAdapter;
import com.wilinz.yuetingmusic.ui.playlist.PlaylistFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FavoriteListFragment extends Fragment {

    private FavoriteListViewModel viewModel;
    private FragmentPlaylistContainerBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPlaylistContainerBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        viewModel = new ViewModelProvider(this).get(FavoriteListViewModel.class);
        PlaylistFragment fragment = (PlaylistFragment) getChildFragmentManager().findFragmentById(R.id.fragment_play_list);
        assert fragment != null;
        fragment.binding.toolbar.setTitle("我的收藏");
        fragment.binding.refresh.setOnRefreshListener(() -> {
            viewModel.getSongsList()
                    .subscribe();
        });

        viewModel.getRefreshingLiveData().observe(getViewLifecycleOwner(), isRefreshing -> {
            fragment.binding.refresh.setRefreshing(isRefreshing != null ? isRefreshing : false);
        });

        viewModel.getSongsLiveData().observe(getViewLifecycleOwner(), songs -> {
            MusicAdapter adapter = (MusicAdapter) fragment.binding.playList.getAdapter();
            adapter.setSongs(songs);
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refresh(FavoriteUpdatedEvent event) {
        viewModel.getSongsList().subscribe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        binding = null;
    }

}