package com.wilinz.yuetingmusic.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.wilinz.yuetingmusic.Key;
import com.wilinz.yuetingmusic.databinding.FragmentSecondBinding;

public class LoginFragment extends Fragment {
    private FragmentSecondBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = NavHostFragment.findNavController(this).getCurrentBackStackEntry().getArguments();
        String email = bundle.getString(Key.email, "");
        binding.emailLogin.getEditText().setText(email);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
