package com.wilinz.yuetingmusic.ui.signinsignup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.wilinz.yuetingmusic.Key;
import com.wilinz.yuetingmusic.Pref;
import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.data.model.User;
import com.wilinz.yuetingmusic.databinding.FragmentLoginBinding;
import com.wilinz.yuetingmusic.util.ToastUtilKt;

public class LoginOrSignupFragment extends Fragment {
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
        viewModel.getSignupResult().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                NavHostFragment.findNavController(this).navigate(R.id.action_LoginFragment_to_MainFragment);
            }
        });
        Bundle bundle = NavHostFragment.findNavController(this).getCurrentBackStackEntry().getArguments();
        if (bundle == null) return;
        User user = bundle.getParcelable(Key.user);
        String email = bundle.getString(Key.username);
        isLoginMode = user != null;
        int textResId = isLoginMode ? R.string.login : R.string.signup;
        binding.loginOrSignup.setText(textResId);
        binding.loginOrSignupLabel.setText(textResId);
        binding.forgetPassword.setVisibility(isLoginMode ? View.VISIBLE : View.GONE);
        binding.loginOrSignup.setOnClickListener(v -> {
            String password = binding.password.getEditText().getText().toString();
            if (!isLoginMode && password.length() < 6) {
                ToastUtilKt.toast(requireContext(), "密码长度必须大于或等于6位");
                return;
            }
            if (isLoginMode) {
                if (viewModel.login(user, password)) {
                    ToastUtilKt.toast(requireContext(), "登录成功");
                    NavHostFragment.findNavController(this).navigate(R.id.action_LoginFragment_to_MainFragment);
                    Pref.getInstance(requireContext()).setFirstLaunch(false);
                } else {
                    ToastUtilKt.toast(requireContext(), "登录失败：密码错误");
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
