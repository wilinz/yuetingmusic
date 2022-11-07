package com.wilinz.yuetingmusic.ui.welcome;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.wilinz.yuetingmusic.Key;
import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.databinding.FragmentFirstBinding;

import java.util.Objects;

public class WelcomeFragment extends Fragment {
    private FragmentFirstBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.continue1.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(Key.email, Objects.requireNonNull(binding.email.getEditText()).getText().toString());
            NavHostFragment.findNavController(this).navigate(R.id.action_FirstFragment_to_SecondFragment, bundle);
        });
        binding.notLoggedIn.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_FirstFragment_to_MainFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
