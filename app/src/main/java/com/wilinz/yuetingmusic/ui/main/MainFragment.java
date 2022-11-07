package com.wilinz.yuetingmusic.ui.main;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.permissionx.guolindev.PermissionX;
import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.util.ScreenUtil;
import com.wilinz.yuetingmusic.databinding.FragmentMainBinding;

public class MainFragment extends Fragment {
    private FragmentMainBinding binding;
    private MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.getSongs().observe(this.getViewLifecycleOwner(), songs -> {
            binding.songs.setText(songs.toString());
        });

        RequestOptions options = new RequestOptions().transform(new RoundedCorners(ScreenUtil.dpToPx(requireContext(), 10)));
        Glide.with(this).load(R.drawable.avatar).apply(options).into(binding.musicAvatar);

        PermissionX.init(requireActivity())
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        viewModel.getMusics(requireContext());
                    } else {
                        Toast.makeText(requireContext(), "这些权限被拒绝: $deniedList", Toast.LENGTH_LONG).show();
                    }
                });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
