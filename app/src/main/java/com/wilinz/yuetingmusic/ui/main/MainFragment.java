package com.wilinz.yuetingmusic.ui.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
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
import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.databinding.FragmentMainBinding;

public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";
    private FragmentMainBinding binding;
    private MainViewModel viewModel;

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
        viewModel.getPlaybackStateLiveData().observe(this.getViewLifecycleOwner(), this::updatePlaybackState);
        viewModel.getMediaMetadataLiveData().observe(this.getViewLifecycleOwner(), this::updateMetadata);
        viewModel.getPlayPositionLiveData().observe(this.getViewLifecycleOwner(), this::updatePosition);
        viewModel.getUpdatePictureRotationLiveData().observe(this.getViewLifecycleOwner(), value -> binding.songAvatar.setRotation(value));
        setViewPage2();
        setBottomNavigation();
        binding.songBar.setOnClickListener((v) -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_MainFragment_to_PlayerFragment);
        });
        binding.playPause.setOnClickListener(v -> {
            if (viewModel.getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED) {
                viewModel.play();
            } else if (viewModel.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                viewModel.pause();
            } else {
                //todo
//                playFromHistory
            }
        });
    }

    private void setViewPage2() {
        binding.viewPage.setAdapter(new ViewPage2Adapter(this));
        binding.viewPage.setUserInputEnabled(false);
        binding.viewPage.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int id = binding.bottomNavigation.getMenu().getItem(position).getItemId();
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
//                case R.id.page_2:
//                    index = 1;
//                    break;
                default:
                    index = 2;
            }
            if (binding.viewPage.getCurrentItem() != index) {
                binding.viewPage.setCurrentItem(index, true);
            }
            return true;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateMetadata(MediaMetadataCompat metadata) {
        Log.d(TAG, "updateMetadata: ");
        if (metadata == null) return;
        MediaDescriptionCompat description = metadata.getDescription();
        Glide.with(requireContext())
                .load(description.getIconUri())
                .into(binding.songAvatar);
        binding.songName.setText(description.getTitle().toString() + " - " + description.getSubtitle());
    }

    private void updatePlaybackState(PlaybackStateCompat state) {
        if (state.getState() == PlaybackStateCompat.STATE_NONE || state.getState() == PlaybackStateCompat.STATE_PAUSED) {
            binding.playPause.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.play_arrow_24px));
            viewModel.stopPictureRotationTimer();
        } else {
            viewModel.startPictureRotationTimer(0.5f);
            binding.playPause.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.round_pause_24));
        }
    }

    private void updatePosition(long position) {
        MediaMetadataCompat mediaMetadata = viewModel.getMediaMetadataLiveData().getValue();
        if (mediaMetadata != null) {
            long duration = mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
            int parent = (int) ((position / (float) duration) * 100 + 0.5);
//            Log.d(TAG, "updatePosition: " + position + "/" + duration + ": " + parent + " %");
//            binding.progressIndicator.setSecondaryProgress(100);
//            binding.progressIndicator.setSecondaryProgressTintMode(true);
//            binding.progressIndicator.
            binding.progressIndicator.setProgress(parent);
        }
    }

}
