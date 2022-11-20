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

import android.os.Handler;
import android.os.Looper;
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
import com.wilinz.yuetingmusic.util.RxTimer;
import com.wilinz.yuetingmusic.util.ScreenUtil;
import com.wilinz.yuetingmusic.util.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;

public class PlayerFragment extends Fragment {

    private final static String TAG = "PlayerFragment";

    private MediaBrowserCompat mediaBrowser;

    private PlayerViewModel viewModel;

    private FragmentPlayerBinding binding;

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
        viewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        // Create MediaBrowserServiceCompat
        mediaBrowser = new MediaBrowserCompat(requireContext(),
                new ComponentName(this.requireContext(), MusicService.class),
                connectionCallbacks,
                null); // optional Bundle
        mediaBrowser.connect();

        binding.playPause.setOnClickListener(v -> {
            int pbState = mediaController.getPlaybackState().getState();
            Log.d(TAG, Integer.valueOf(pbState).toString());
            if (pbState == PlaybackStateCompat.STATE_PAUSED) {
                getTransportControls().play();
            } else if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                getTransportControls().pause();
            }
        });
        binding.skipToPrevious.setOnClickListener(v -> {
            getTransportControls().skipToPrevious();
        });
        binding.skipToNext.setOnClickListener(v -> {
            getTransportControls().skipToNext();
        });
        binding.currentProgress.setLabelFormatter((value) -> TimeUtil.setTimeByZero((long) value));
        binding.currentProgress.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) getTransportControls().seekTo((long) value);
        });
    }

    private void setBottomPadding() {
        ViewGroup.LayoutParams layoutParams = binding.bottomPadding.getLayoutParams();
        layoutParams.height = ScreenUtil.getNavigationBarHeight(requireContext());
    }

    private MediaControllerCompat.TransportControls getTransportControls() {
        return mediaController.getTransportControls();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) timer.cancel();
        //（请参阅“与 MediaSession 保持同步”）
        if (mediaController != null) {
            mediaController.unregisterCallback(controllerCallback);
            controllerCallback = null;
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
                    mediaController =
                            new MediaControllerCompat(requireContext(), // Context
                                    token);

                    // Save the controller

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

    MediaControllerCompat mediaController;

    private void buildTransportControls() {
        // 由于这是一个播放暂停按钮，因此您需要测试当前状态并相应地选择操作
        // 显示初始状态
        updateMetadata(mediaController.getMetadata());
        updatePlaybackState(mediaController.getPlaybackState());
        updatePosition();
        // 注册回调以保持同步
        controllerCallback =
                new MediaControllerCompat.Callback() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onMetadataChanged(MediaMetadataCompat metadata) {
                        if (metadata == null) return;
                        updateMetadata(metadata);
                    }

                    @Override
                    public void onPlaybackStateChanged(PlaybackStateCompat state) {
                        updatePlaybackState(state);
                    }
                };
        mediaController.registerCallback(controllerCallback);
    }

    MediaControllerCompat.Callback controllerCallback;

    private void updatePlaybackState(PlaybackStateCompat state) {
        if (state.getState() == PlaybackStateCompat.STATE_NONE || state.getState() == PlaybackStateCompat.STATE_PAUSED) {
            binding.playPause.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.play_arrow_24px));
            if (timer != null) timer.cancel();
        } else {
            binding.playPause.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.round_pause_24));
            timer = new RxTimer();
            timer.interval(1000, (number) -> updatePosition());
        }
    }

    private void updatePosition() {
        long position = mediaController.getPlaybackState().getPosition();
        if (binding != null) {
            binding.currentProgress.setValue(position);
            binding.currentProgressTime.setText(TimeUtil.setTimeByZero(position));
        }
    }

    private void updateMetadata(MediaMetadataCompat metadata) {
        if (metadata == null) return;
        MediaDescriptionCompat description = metadata.getDescription();
        binding.songName.setText(description.getTitle().toString() + " - " + description.getSubtitle());
        long duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        if (duration > 0) {
            binding.currentProgress.setValueTo(duration);
            binding.currentProgressTime.setText(TimeUtil.setTimeByZero(duration));
        }
    }

    private RxTimer timer;
}