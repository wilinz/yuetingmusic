package com.wilinz.yuetingmusic.ui.main.home;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.permissionx.guolindev.PermissionX;
import com.wilinz.yuetingmusic.Key;
import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.databinding.FragmentHomeBinding;

import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;

    private static String TAG = "HomeFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        viewModel.getEvent().observe(this.getViewLifecycleOwner(), (event) -> {
            if (event == Event.GetMusicsSuccess && binding.swipeRefresh.isRefreshing()) {
                binding.swipeRefresh.setRefreshing(false);
            }
        });
        viewModel.getSongs().observe(this.getViewLifecycleOwner(), songs -> {
            MusicAdapter adapter = (MusicAdapter) binding.musicList.getAdapter();
            Log.d(TAG, songs.toString());
            adapter.setMusics(songs);
        });

        MusicAdapter adapter = new MusicAdapter(List.of());
        adapter.setOnItemClickListener((index, music) -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable(Key.music, music);
            NavHostFragment.findNavController(this).navigate(R.id.action_to_PlayerFragment, bundle);
        });
        binding.musicList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.musicList.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            getMusics();
        });

        getMusics();
    }

    public void getMusics() {
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