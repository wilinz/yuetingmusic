package com.wilinz.yuetingmusic.ui.player;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.wilinz.yuetingmusic.Key;
import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.databinding.FragmentPlayerBinding;
import com.wilinz.yuetingmusic.service.PlayerEvent;
import com.wilinz.yuetingmusic.service.PlayerService;
import com.wilinz.yuetingmusic.util.ScreenUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

public class PlayerFragment extends Fragment {

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
        EventBus.getDefault().register(this);
        PlayerService.start(requireContext());
        viewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        ViewGroup.LayoutParams layoutParams = binding.bottomPadding.getLayoutParams();
        layoutParams.height = ScreenUtil.getNavigationBarHeight(requireContext());
        Bundle bundle = NavHostFragment.findNavController(this).getCurrentBackStackEntry().getArguments();
        Song music = bundle.getParcelable(Key.music);

        EventBus.getDefault().post(new PlayerEvent.GetPlayStatusEvent());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        binding.play.setOnClickListener(v -> {
            Object tag = binding.play.getTag();
            boolean isPlayed = tag instanceof Boolean && (boolean) tag;
            if (!isPlayed) {
                EventBus.getDefault().post(new PlayerEvent.PlayEvent(music));
            } else {
                EventBus.getDefault().post(new PlayerEvent.PauseEvent());
            }
        });
        binding.currentProgress.setLabelFormatter((value) -> dateFormat.format(value));
        binding.currentProgress.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                EventBus.getDefault().post(new PlayerEvent.SeekEvent((long) value));
            }
        });
        binding.musicName.setText(music.song);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayEvent(PlayerEvent.PlayEvent event) {
        setPlayButtonStatus(true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPauseEvent(PlayerEvent.PauseEvent event) {
        setPlayButtonStatus(false);
    }

    private void setPlayButtonStatus(boolean isPlaying) {
        Object tag = binding.play.getTag();
        boolean tag1 = tag instanceof Boolean && (boolean) tag;
        if (tag1 != isPlaying) {
            binding.play.setTag(isPlaying);
            if (isPlaying) {
                binding.play.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.round_pause_24));
            } else {
                binding.play.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.round_play_arrow_24));
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        binding = null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProgressChangeEvent(PlayerEvent.ProgressChangeEvent event) {
        long duration = event.duration > 0 ? event.duration : 60000;
        long progress = event.progress >= 0 ? event.progress : 0;
        binding.currentProgressTime.setText(dateFormat.format(progress));
        binding.duration.setText(dateFormat.format(duration));
        binding.currentProgress.setValueTo(duration);
        binding.currentProgress.setValue(progress);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCreateMediaPlayerEvent(PlayerEvent.CreateMediaPlayerEvent event) {
//        setPlayButtonStatus(true);
//        Toast.makeText(requireContext(), "创建播放器", Toast.LENGTH_SHORT).show();
        setPlayButtonStatus(event.player.isPlaying());
    }
}