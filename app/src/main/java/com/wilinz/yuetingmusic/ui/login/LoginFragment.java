package com.wilinz.yuetingmusic.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.wilinz.yuetingmusic.Key;
import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.data.model.User;
import com.wilinz.yuetingmusic.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private boolean isLoginMode = false;
    private LoginOrSignupViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginOrSignupViewModel.class);
        viewModel.getSignupResult().observe(getViewLifecycleOwner(),success->{
            if (success){
                NavHostFragment.findNavController(this).navigate(R.id.action_LoginFragment_to_MainFragment);
            }
        });
        Bundle bundle = NavHostFragment.findNavController(this).getCurrentBackStackEntry().getArguments();
        User user = bundle.getParcelable(Key.user);
        String email = bundle.getString(Key.email);
        isLoginMode = user != null;
        int textResId = isLoginMode ? R.string.login : R.string.signup;
        binding.loginOrSignup.setText(textResId);
        binding.loginOrSignupLabel.setText(textResId);
        binding.forgetPassword.setVisibility(isLoginMode ? View.VISIBLE : View.GONE);
        binding.loginOrSignup.setOnClickListener(v -> {
            String password = binding.password.getEditText().getText().toString();
            if (isLoginMode) {
                if (viewModel.login(user, password)) {
                    Toast.makeText(requireContext(), "登录成功", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).navigate(R.id.action_LoginFragment_to_MainFragment);
                } else {
                    Toast.makeText(requireContext(), "登录失败：密码错误", Toast.LENGTH_SHORT).show();
                }
            } else {
                viewModel.signup(email, password);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
