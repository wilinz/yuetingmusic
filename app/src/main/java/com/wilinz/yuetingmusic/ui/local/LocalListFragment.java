package com.wilinz.yuetingmusic.ui.local;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.permissionx.guolindev.PermissionX;
import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle;
import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.databinding.FragmentPlaylistContainerBinding;
import com.wilinz.yuetingmusic.ui.commen.MusicAdapter;
import com.wilinz.yuetingmusic.ui.playlist.PlaylistFragment;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

public class LocalListFragment extends Fragment {

    private LocalListViewModel viewModel;
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
        viewModel = new ViewModelProvider(this).get(LocalListViewModel.class);
        PlaylistFragment fragment = (PlaylistFragment) getChildFragmentManager().findFragmentById(R.id.fragment_play_list);
        assert fragment != null;
        fragment.binding.toolbar.setTitle("本地音乐");
        fragment.binding.refresh.setOnRefreshListener(() -> {
            getMusics();
        });

        viewModel.getRefreshingLiveData().observe(getViewLifecycleOwner(), isRefreshing -> {
            fragment.binding.refresh.setRefreshing(isRefreshing != null ? isRefreshing : false);
        });

        viewModel.getSongsLiveData().observe(getViewLifecycleOwner(), songs -> {
            MusicAdapter adapter = (MusicAdapter) fragment.binding.playList.getAdapter();
            adapter.setSongs(songs);
        });

        if (viewModel.getSongsLiveData().getValue() == null) getMusics();
    }

    public void getMusics() {
        PermissionX.init(requireActivity())
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        viewModel.getSongsList().subscribe();
                    } else {
                        Toast.makeText(requireContext(), "这些权限被拒绝: $deniedList", Toast.LENGTH_LONG).show();
                    }
                });
    }

}