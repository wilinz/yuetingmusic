package com.wilinz.yuetingmusic.ui.user;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wilinz.yuetingmusic.Key;
import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.data.repository.UserRepository;
import com.wilinz.yuetingmusic.databinding.FragmentUserListBinding;

import java.util.List;

public class UserListFragment extends Fragment {

    private UserListViewModel viewModel;
    private FragmentUserListBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUserListBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        UserListAdapter adapter = new UserListAdapter(List.of());
        adapter.setOnItemClickListener((users, index, user) -> {
            if (user.rememberPassword) {
                viewModel.changeActive(user, true).subscribe();
            } else {
                Bundle bundle = new Bundle();
                bundle.putParcelable(Key.user, user);
                bundle.putString(Key.username, user.username);
                NavHostFragment.findNavController(this).navigate(R.id.action_UserListFragment_to_LoginFragment, bundle);
            }
        });
        viewModel = new ViewModelProvider(this).get(UserListViewModel.class);
        viewModel.getUsersLiveDate().observe(getViewLifecycleOwner(), users -> {
            adapter.setUsers(users);
        });
        binding.userList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.userList.setAdapter(adapter);
        binding.refresh.setOnRefreshListener(() -> {
            viewModel.getAllUser().subscribe();
        });
        binding.toolbar.setTitle("所有用户");
        binding.addUser.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_UserListFragment_to_WelcomeFragment);
        });
//        binding.exitLogin.setOnClickListener(v->{
//            viewModel.exitLogin().subscribe();
//        });
        viewModel.getRefreshingLiveData().observe(getViewLifecycleOwner(), isRefreshing -> {
            binding.refresh.setRefreshing(isRefreshing != null ? isRefreshing : false);
        });
    }
}