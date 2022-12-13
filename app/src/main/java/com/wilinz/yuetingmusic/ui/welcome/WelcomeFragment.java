package com.wilinz.yuetingmusic.ui.welcome;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle;
import com.wilinz.yuetingmusic.Key;
import com.wilinz.yuetingmusic.Pref;
import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.databinding.FragmentWelcomeBinding;
import com.wilinz.yuetingmusic.util.ToastUtilKt;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

public class WelcomeFragment extends Fragment {
    private FragmentWelcomeBinding binding;
    private WelcomeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(WelcomeViewModel.class);
        binding.continue1.setOnClickListener(v -> {
            String username = binding.username.getEditText().getText().toString().trim();
            if (TextUtils.isEmpty(username)) {
                ToastUtilKt.toast(requireContext(), "用户名不能为空");
                return;
            }
            viewModel.getUser(username)
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(AndroidLifecycle.createLifecycleProvider(getViewLifecycleOwner()).bindToLifecycle())
                    .subscribe(
                            (user -> {
                                Bundle bundle = new Bundle();
                                bundle.putParcelable(Key.user, user.orElse(null));
                                bundle.putString(Key.email, binding.username.getEditText().getText().toString());
                                NavHostFragment.findNavController(this).navigate(R.id.action_WelcomeFragment_to_LoginFragment, bundle);
                            }),
                            err -> {
                                err.printStackTrace();
                                Toast.makeText(requireContext(), "登录或注册失败：" + err, Toast.LENGTH_LONG).show();
                            });
        });
        binding.notLoggedIn.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            if (navController.getPreviousBackStackEntry() == null) {
                navController.navigate(R.id.action_FirstFragment_to_MainFragment);
            } else {
                navController.popBackStack();
            }
            Pref.getInstance(requireContext()).setFirstLaunch(false);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
