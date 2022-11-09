package com.wilinz.yuetingmusic.ui.player;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import java.util.Locale;

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
        requireContext().startService(new Intent(requireContext(), PlayerService.class));
        viewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        ViewGroup.LayoutParams layoutParams = binding.bottomPadding.getLayoutParams();
        layoutParams.height = ScreenUtil.getNavigationBarHeight(requireContext());
        Bundle bundle = NavHostFragment.findNavController(this).getCurrentBackStackEntry().getArguments();
        Song music = bundle.getParcelable(Key.music);

        binding.play.setTag(false);
        binding.play.setOnClickListener(v -> {
            boolean isPlayed = (boolean) binding.play.getTag();
            setPlayButtonStatus(isPlayed);
            if (!isPlayed) {
                EventBus.getDefault().post(new PlayerEvent.PlayEvent(music));
            } else {
                EventBus.getDefault().post(new PlayerEvent.PauseEvent());
            }
        });
        binding.currentProgress.setLabelFormatter((value) -> dateFormat.format(value));
        binding.currentProgress.addOnChangeListener((slider, value, fromUser) -> {
            EventBus.getDefault().post(new PlayerEvent.SeekEvent((long) value));
        });
        binding.musicName.setText(music.song);
    }

    private void setPlayButtonStatus(boolean isPlayed) {
        if (!isPlayed) {
            binding.play.setTag(true);
            binding.play.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.round_pause_24));
        } else {
            binding.play.setTag(false);
            binding.play.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.round_play_arrow_24));
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
//        setPlayButtonStatus(true);
        binding.currentProgressTime.setText(dateFormat.format(event.progress));
        binding.duration.setText(dateFormat.format(event.duration));
        binding.currentProgress.setValueTo(event.duration);
        binding.currentProgress.setValue(event.progress);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCreateMediaPlayerEvent(PlayerEvent.CreateMediaPlayerEvent event) {
        Toast.makeText(requireContext(),"创建播放器",Toast.LENGTH_SHORT).show();
        setPlayButtonStatus(event.player.isPlaying());
    }
}