package com.wilinz.yuetingmusic.ui.player;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.wilinz.yuetingmusic.Key;
import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.databinding.FragmentPlayerBinding;
import com.wilinz.yuetingmusic.service.MusicService;
import com.wilinz.yuetingmusic.service.PlayerEvent;
import com.wilinz.yuetingmusic.service.PlayerService;
import com.wilinz.yuetingmusic.util.ScreenUtil;
import com.wilinz.yuetingmusic.util.TimeUtil;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class PlayerFragment extends Fragment {

    private final static String TAG = "PlayerFragment";

    private MediaBrowserCompat mediaBrowser;

    private PlayerViewModel viewModel;

    private FragmentPlayerBinding binding;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.UK);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPlayerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setBottomPadding();
        Bundle bundle = NavHostFragment.findNavController(this).getCurrentBackStackEntry().getArguments();
        Song music = bundle.getParcelable(Key.music);
        viewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        // Create MediaBrowserServiceCompat
        mediaBrowser = new MediaBrowserCompat(requireContext(),
                new ComponentName(this.requireContext(), MusicService.class),
                connectionCallbacks,
                null); // optional Bundle
        mediaBrowser.connect();

        binding.playPause.setOnClickListener(v -> {
            int pbState = getMediaController().getPlaybackState().getState();
            Log.d(TAG, Integer.valueOf(pbState).toString());
            if (pbState == PlaybackStateCompat.STATE_PAUSED) {
                getTransportControls().play();
            } else if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                getTransportControls().pause();
            } else {
                getTransportControls().playFromUri(Uri.parse(music.path), null);
            }
        });
        binding.currentProgress.setLabelFormatter((value) -> dateFormat.format(value));
        binding.currentProgress.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) getTransportControls().seekTo((long) value);
        });
    }

    private void setBottomPadding() {
        ViewGroup.LayoutParams layoutParams = binding.bottomPadding.getLayoutParams();
        layoutParams.height = ScreenUtil.getNavigationBarHeight(requireContext());
    }

    private MediaControllerCompat.TransportControls getTransportControls() {
        return getMediaController().getTransportControls();
    }

    private MediaControllerCompat getMediaController() {
        return MediaControllerCompat.getMediaController(requireActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //（请参阅“与 MediaSession 保持同步”）
        if (MediaControllerCompat.getMediaController(requireActivity()) != null) {
            MediaControllerCompat.getMediaController(requireActivity()).unregisterCallback(controllerCallback);
        }
        mediaBrowser.disconnect();

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
        // 由于这是一个播放暂停按钮，因此您需要测试当前状态并相应地选择操作

        int pbState = MediaControllerCompat.getMediaController(requireActivity()).getPlaybackState().getState();
        if (pbState == PlaybackStateCompat.STATE_PLAYING) {
            MediaControllerCompat.getMediaController(requireActivity()).getTransportControls().pause();
        } else {
            MediaControllerCompat.getMediaController(requireActivity()).getTransportControls().play();
        }

        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(requireActivity());

        // 显示初始状态
        MediaMetadataCompat metadata = mediaController.getMetadata();
        PlaybackStateCompat pbState1 = mediaController.getPlaybackState();

        // 注册回调以保持同步
        mediaController.registerCallback(controllerCallback);
    }

    MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    if (metadata == null) return;
                    MediaDescriptionCompat description = metadata.getDescription();
                    binding.songName.setText(description.getTitle().toString() + " - " + description.getSubtitle());
                    long duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
                    if (duration > 0) {
                        binding.currentProgress.setValueTo(duration);
                        binding.currentProgressTime.setText(TimeUtil.setTimeByZero(duration));
                    }
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    if (state.getState() == PlaybackStateCompat.STATE_NONE || state.getState() == PlaybackStateCompat.STATE_PAUSED) {
                        binding.playPause.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.play_arrow_24px));
                    } else {
                        binding.playPause.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.round_pause_24));
                    }

                    long duration = state.getExtras().getLong(Key.duration, 60000);
                    duration = duration > 0 ? duration : 60000;
                    long progress = state.getPosition();
//                            >= 0 ? event.progress : 0;
                    binding.currentProgressTime.setText(dateFormat.format(progress));
                    binding.duration.setText(dateFormat.format(duration));
                    binding.currentProgress.setValueTo(duration);
                    binding.currentProgress.setValue(progress);
                }
            };

}