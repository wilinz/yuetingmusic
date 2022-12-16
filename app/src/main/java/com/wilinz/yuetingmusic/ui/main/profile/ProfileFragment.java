package com.wilinz.yuetingmusic.ui.main.profile;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle;
import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.databinding.FragmentProfileBinding;
import com.wilinz.yuetingmusic.event.RecentRecordUpdatedEvent;
import com.wilinz.yuetingmusic.ui.commen.MusicAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

    ActivityResultLauncher<String> getContentLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri == null) return;
                viewModel.setUserAvatar(viewModel.getUserLiveData().getValue(), uri)
                        .compose(AndroidLifecycle.createLifecycleProvider(getViewLifecycleOwner()).bindToLifecycle())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(user -> {

                                },
                                err -> {
                                    err.printStackTrace();
                                    Toast.makeText(requireContext(), "设置头像失败：" + err.toString(), Toast.LENGTH_SHORT).show();
                                },
                                () -> {
                                    Toast.makeText(requireContext(), "设置头像成功", Toast.LENGTH_SHORT).show();
                                });
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.username.setText(user.username);
                binding.nickname.setText(user.nickname);
                if (user.avatar != null) setAvatarView(Uri.parse(user.avatar));
            }
        });
        MusicAdapter adapter = new MusicAdapter(List.of());
        viewModel.getRecentListLiveData().observe(getViewLifecycleOwner(), songs -> {
            adapter.setSongs(songs);
        });
        binding.user.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_MainFragment_to_UserListFragment);
        });
        binding.avatar.setOnClickListener(v -> {
            getContentLauncher.launch("image/*");
        });
        binding.favorite.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_MainFragment_to_FavoriteListFragment);
        });
        binding.localMusic.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_MainFragment_to_LocalMusicFragment);
        });
        binding.playHistory.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_MainFragment_to_RecentListFragment);
        });
        binding.settings.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_MainFragment_to_SettingsFragment);
        });
        binding.recentPlay.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter.setOnItemClickListener((songs, index, song) -> {
            viewModel.playFromUri(songs, song);
        });
        binding.recentPlay.setAdapter(adapter);
    }

    private void setAvatarView(@NonNull Uri uri) {
        try {
            binding.avatar.setImageURI(uri);
        } catch (Exception e) {
            e.printStackTrace();
            binding.avatar.setImageResource(R.drawable.avatar2);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
