package com.wilinz.yuetingmusic.ui.user;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.wilinz.yuetingmusic.R;
import com.wilinz.yuetingmusic.data.model.User;
import com.wilinz.yuetingmusic.databinding.ItemUserBinding;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {

    private List<User> users;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(List<User> users, int index, User user);
    }

    public UserListAdapter(@NonNull List<User> users) {
        this.users = users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        listener = l;
    }

    static class UserListViewHolder extends RecyclerView.ViewHolder {

        ItemUserBinding binding;

        public UserListViewHolder(@NonNull ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public UserListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        UserListViewHolder holder = new UserListViewHolder(binding);
        binding.getRoot().setOnClickListener(v -> {
            if (listener != null) {
                int index = holder.getAbsoluteAdapterPosition();
                listener.onItemClick(users, index, users.get(index));
            }
        });
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull UserListViewHolder holder, int position) {
        ItemUserBinding binding = holder.binding;
        User user = users.get(position);
        binding.name.setText(user.username);
//        binding.serialNumber.setText((position + 1) + "");
        binding.secondName.setText(user.nickname);
        if (user.isActive){
            binding.tag.setText("当前用户");
        }else {
            binding.tag.setText("");
        }
        Glide.with(binding.songAvatar)
                .load(user.avatar)
                .error(R.drawable.avatar2)
                .into(binding.songAvatar);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


}
