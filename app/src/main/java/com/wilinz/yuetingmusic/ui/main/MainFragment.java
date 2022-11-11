package com.wilinz.yuetingmusic.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        RequestOptions options = new RequestOptions().transform(new RoundedCorners(ScreenUtil.dpToPx(requireContext(), 10)));
        Glide.with(this).load(R.drawable.avatar).apply(options).into(binding.musicAvatar);

        binding.viewPage.setAdapter(new ViewPage2Adapter(this));
        binding.viewPage.setUserInputEnabled(false);
        binding.viewPage.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int id;
                switch (position) {
                    case 0:
                        id = R.id.page_1;
                        break;
                    case 1:
                        id = R.id.page_2;
                        break;
                    default:
                        id = R.id.page_3;
                }
                if (binding.bottomNavigation.getSelectedItemId() != id) {
                    binding.bottomNavigation.setSelectedItemId(id);
                }
            }

        });

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int index;
            switch (item.getItemId()) {
                case R.id.page_1:
                    index = 0;
                    break;
                case R.id.page_2:
                    index = 1;
                    break;
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
}
