package com.wilinz.yuetingmusic.ui.main;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.databinding.FragmentMainBinding;
import com.wilinz.yuetingmusic.service.MusicService;
import com.wilinz.yuetingmusic.util.ScreenUtil;

import java.util.Objects;

public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";
    private FragmentMainBinding binding;
    private MainViewModel viewModel;
    private MediaBrowserCompat mediaBrowser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        mediaBrowser = new MediaBrowserCompat(requireContext(),
                new ComponentName(this.requireContext(), MusicService.class),
                connectionCallbacks,
                null); // optional Bundle
        mediaBrowser.connect();

        RequestOptions options = new RequestOptions().transform(new RoundedCorners(ScreenUtil.dpToPx(requireContext(), 10)));
        Glide.with(this).load(R.drawable.avatar).apply(options).into(binding.songAvatar);

        setViewPage2();
        setBottomNavigation();
        binding.songBar.setOnClickListener((v) -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_MainFragment_to_PlayerFragment);
        });
    }

    private void setViewPage2() {
        binding.viewPage.setAdapter(new ViewPage2Adapter(this));
        binding.viewPage.setUserInputEnabled(false);
        binding.viewPage.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int id;
                switch (position) {
                    case 0:
                        id = R.id.page_1;
                        break;
                    case 1:
                        id = R.id.page_2;
                        break;
                    default:
                        id = R.id.page_3;
                }
                if (binding.bottomNavigation.getSelectedItemId() != id) {
                    binding.bottomNavigation.setSelectedItemId(id);
                }
            }

        });
    }

    @SuppressLint("NonConstantResourceId")
    private void setBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int index;
            switch (item.getItemId()) {
                case R.id.page_1:
                    index = 0;
                    break;
                case R.id.page_2:
                    index = 1;
                    break;
                default:
                    index = 2;
            }
            if (binding.viewPage.getCurrentItem() != index) {
                binding.viewPage.setCurrentItem(index, true);
            }
            return true;
        });
    }

    private MediaControllerCompat.TransportControls getTransportControls() {
        return getMediaController().getTransportControls();
    }

    private MediaControllerCompat getMediaController() {
        return MediaControllerCompat.getMediaController(requireActivity());
    }


    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView: ");
        //（请参阅“与 MediaSession 保持同步”）
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(requireActivity());
        if (controller != null) {
            Log.d(TAG, "unregisterCallback");
            controller.unregisterCallback(controllerCallback);
            controllerCallback=null;
        }
        mediaBrowser.disconnect();
        super.onDestroyView();
        binding = null;
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
                    buildTransportControls();
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

    private void buildTransportControls() {
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(requireActivity());
        updateUi(mediaController.getMetadata(), mediaController.getPlaybackState());
        // 注册回调以保持同步
        Log.d(TAG, "registerCallback");
        mediaController.registerCallback(controllerCallback);
    }

    private void updateUi(MediaMetadataCompat metadata, PlaybackStateCompat state) {
        updateMetadata(metadata);
        updatePlaybackState(state);
    }

    private void updateMetadata(MediaMetadataCompat metadata) {
        if (metadata == null) return;
        binding.songName.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
    }

    private void updatePlaybackState(PlaybackStateCompat state) {
        if (state.getState() == PlaybackStateCompat.STATE_NONE || state.getState() == PlaybackStateCompat.STATE_PAUSED) {
            binding.playPause.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.play_arrow_24px));
        } else {
            binding.playPause.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.round_pause_24));
        }
    }

    MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    updateMetadata(metadata);
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    Log.d(TAG, "onPlaybackStateChanged");
                    updatePlaybackState(state);
                }
            };
}
