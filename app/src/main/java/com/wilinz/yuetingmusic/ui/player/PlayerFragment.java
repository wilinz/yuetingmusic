package com.wilinz.yuetingmusic.ui.player;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.wilinz.yuetingmusic.Key;
import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.constant.PlayMode;
import com.wilinz.yuetingmusic.databinding.FragmentPlayerBinding;
import com.wilinz.yuetingmusic.player.MyNotificationManager;
import com.wilinz.yuetingmusic.util.LogUtil;
import com.wilinz.yuetingmusic.util.ScreenUtil;
import com.wilinz.yuetingmusic.util.TimeUtil;
import com.wilinz.yuetingmusic.util.UriUtil;

import java.util.Objects;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class PlayerFragment extends Fragment {

    private final static String TAG = "PlayerFragment";
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

        viewModel.getPlayPositionLiveData().observe(this.getViewLifecycleOwner(), this::updatePosition);
        viewModel.getPlaybackStateLiveData().observe(this.getViewLifecycleOwner(), this::updatePlaybackState);
        viewModel.getMediaMetadataLiveData().observe(this.getViewLifecycleOwner(), this::updateMetadata);
        viewModel.getPlayModeLiveData().observe(this.getViewLifecycleOwner(), this::updatePlayMode);
        viewModel.getUpdatePictureRotationLiveData().observe(this.getViewLifecycleOwner(), value -> binding.avatar.setRotation(value));
        binding.playPause.setOnClickListener(v -> {
            int pbState = viewModel.getPlaybackState().getState();
            LogUtil.d(TAG, Integer.valueOf(pbState).toString());
            if (pbState == PlaybackStateCompat.STATE_PAUSED) {
                viewModel.play();
            } else if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                viewModel.pause();
            }
        });
        binding.skipToPrevious.setOnClickListener(v -> {
            viewModel.skipToPrevious();
        });
        binding.skipToNext.setOnClickListener(v -> {
            viewModel.skipToNext();
        });
//        binding.currentProgress.setLabelFormatter((value) -> TimeUtil.format((long) value));
//        binding.currentProgress.addOnChangeListener((slider, value, fromUser) -> {
//            if (fromUser) viewModel.seekTo((long) value);
//        });
        binding.currentProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                viewModel.seekTo(seekBar.getProgress());
            }
        });
        binding.switchPlayMode.setOnClickListener(v -> {
            viewModel.switchPlayMode();
        });
        binding.favorite.setOnClickListener(v -> {
            viewModel.saveFavoriteSong();
        });
    }

    private void setBottomPadding() {
        ViewGroup.LayoutParams layoutParams = binding.bottomPadding.getLayoutParams();
        layoutParams.height = ScreenUtil.getNavigationBarHeight(requireContext());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    private void updatePlayMode(int playMode) {
        int resId = 0;
        if (playMode == PlayMode.ORDERLY) {
            resId = R.drawable.round_repeat_24;
        } else if (playMode == PlayMode.SINGLE_LOOP) {
            resId = R.drawable.round_repeat_one_24;
        } else {
            resId = R.drawable.round_shuffle_24;
        }
        binding.switchPlayMode.setIcon(ContextCompat.getDrawable(requireContext(), resId));
    }

    private void updatePlaybackState(PlaybackStateCompat state) {
        if (state.getState() == PlaybackStateCompat.STATE_NONE || state.getState() == PlaybackStateCompat.STATE_PAUSED) {
            binding.playPause.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.play_arrow_24px));
            viewModel.stopPictureRotationTimer();
        } else {
            viewModel.startPictureRotationTimer(0.25f);
            binding.playPause.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.round_pause_24));
        }
    }

    private void updatePosition(long position) {
        if (position <= binding.currentProgress.getMax()) {
            try {
                binding.currentProgress.setProgress((int) position);
                binding.currentProgressTime.setText(TimeUtil.format(position));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateMetadata(MediaMetadataCompat metadata) {
        if (metadata == null) return;
        MediaDescriptionCompat description = metadata.getDescription();
        binding.name.setText(description.getTitle() + " - " + description.getSubtitle());

        if (!description.getIconUri().equals(UriUtil.idToUri(requireContext(), R.drawable.icon))) {
            Glide.with(requireContext())
                    .load(description.getIconUri())
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 3)))
                    .into(binding.backgroundImage);
        }

        /*    Glide.with(requireContext())
                .asBitmap()
                .load(description.getIconUri())
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 3)))
                .into(new CustomTarget<Bitmap>(binding.backgroundImage.getWidth(), binding.backgroundImage.getHeight()) {
            @Override
            public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                Palette.from(bitmap)
                        .
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });*/
        Glide.with(requireContext())
                .load(description.getIconUri())
                .error(R.drawable.logo)
                .into(binding.avatar);
        long duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        Log.d(TAG, "updateMetadata: " + duration + ", " + TimeUtil.format(duration));
        if (duration > 0) {
            Log.d(TAG, "updateMetadata2: " + duration + ", " + TimeUtil.format(duration));
//            binding.currentProgress.setProgress(0);
            binding.currentProgress.setMax((int) duration);
            binding.duration.setText(TimeUtil.format(duration));
        }
    }


}