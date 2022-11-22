package com.wilinz.yuetingmusic.ui.player;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.databinding.FragmentPlayerBinding;
import com.wilinz.yuetingmusic.ui.commen.MediaControllerViewModel;
import com.wilinz.yuetingmusic.util.ScreenUtil;
import com.wilinz.yuetingmusic.util.TimeUtil;

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

        binding.playPause.setOnClickListener(v -> {
            int pbState = viewModel.getPlaybackState().getState();
            Log.d(TAG, Integer.valueOf(pbState).toString());
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
        binding.currentProgress.setLabelFormatter((value) -> TimeUtil.format((long) value));
        binding.currentProgress.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) viewModel.seekTo((long) value);
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

    private void updatePlaybackState(PlaybackStateCompat state) {
        if (state.getState() == PlaybackStateCompat.STATE_NONE || state.getState() == PlaybackStateCompat.STATE_PAUSED) {
            binding.playPause.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.play_arrow_24px));
        } else {
            binding.playPause.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.round_pause_24));
        }
    }

    private void updatePosition(long position) {
        if (position > binding.currentProgress.getValueTo()) {
            binding.currentProgress.setValueTo(position);
        }
        binding.currentProgress.setValue(position);
        binding.currentProgressTime.setText(TimeUtil.format(position));
    }

    private void updateMetadata(MediaMetadataCompat metadata) {
        if (metadata == null) return;
        MediaDescriptionCompat description = metadata.getDescription();
        binding.songName.setText(description.getTitle().toString() + " - " + description.getSubtitle());
        long duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        if (duration > 0) {
            binding.currentProgress.setValueTo(duration);
            binding.currentProgressTime.setText(TimeUtil.format(duration));
        }
    }


}