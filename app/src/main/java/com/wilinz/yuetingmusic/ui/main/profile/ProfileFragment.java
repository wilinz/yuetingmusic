package com.wilinz.yuetingmusic.ui.main.profile;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle;
import com.wilinz.yuetingmusic.Key;
import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.data.model.User;
import com.wilinz.yuetingmusic.databinding.FragmentProfileBinding;

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
                binding.username.setText(user.email);
                if (user.avatar != null) setAvatarView(Uri.parse(user.avatar));
            }
        });
        binding.user.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_MainFragment_to_WelcomeFragment);
        });
        binding.avatar.setOnClickListener(v -> {
            getContentLauncher.launch("image/*");
        });
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
