package com.wilinz.yuetingmusic.ui.main.home;

import android.Manifest;
import android.content.ComponentName;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.permissionx.guolindev.PermissionX;
import com.wilinz.yuetingmusic.databinding.FragmentHomeBinding;
import com.wilinz.yuetingmusic.service.MusicService;
import com.wilinz.yuetingmusic.service.PlayQueue;

import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;

    private static String TAG = "HomeFragment";

    private MediaBrowserCompat mediaBrowser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        viewModel.getEvent().observe(this.getViewLifecycleOwner(), (event) -> {
            if (event == Event.GetMusicsSuccess && binding.swipeRefresh.isRefreshing()) {
                binding.swipeRefresh.setRefreshing(false);
            }
        });
        viewModel.getSongs().observe(this.getViewLifecycleOwner(), songs -> {
            MusicAdapter adapter = (MusicAdapter) binding.musicList.getAdapter();
            Log.d(TAG, songs.toString());
            assert adapter != null;
            adapter.setSongs(songs);
        });

        mediaBrowser = new MediaBrowserCompat(requireContext(),
                new ComponentName(this.requireContext(), MusicService.class),
                connectionCallbacks,
                null); // optional Bundle
        mediaBrowser.connect();

        MusicAdapter adapter = new MusicAdapter(List.of());
        adapter.setOnItemClickListener((songs, index, song) -> {
            PlayQueue.getInstance().set(songs);
            MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(requireActivity());
            mediaController.getTransportControls().playFromUri(song.uri,null);
        });
        binding.musicList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.musicList.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::getMusics);

        getMusics();
    }

    private final MediaBrowserCompat.ConnectionCallback connectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    // Get the token for the MediaSession
                    MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
                    // Create a MediaControllerCompat
                    MediaControllerCompat mediaController =
                            new MediaControllerCompat(requireContext(), // Context
                                    token);
                    // Save the controller
                    MediaControllerCompat.setMediaController(requireActivity(), mediaController);
                    // Finish building the UI
                }

                @Override
                public void onConnectionSuspended() {
                    // 服务已崩溃。禁用传输控制，直到它自动重新连接
                }

                @Override
                public void onConnectionFailed() {
                    // 该服务已拒绝我们的连接
                }
            };

    public void getMusics() {
        PermissionX.init(requireActivity())
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        viewModel.getMusics(requireContext());
                    } else {
                        Toast.makeText(requireContext(), "这些权限被拒绝: $deniedList", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
