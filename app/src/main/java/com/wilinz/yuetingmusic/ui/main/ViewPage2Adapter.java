package com.wilinz.yuetingmusic.ui.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.wilinz.yuetingmusic.ui.main.favorite.FavoriteFragment;
import com.wilinz.yuetingmusic.ui.main.home.HomeFragment;
import com.wilinz.yuetingmusic.ui.main.profile.ProfileFragment;

public class ViewPage2Adapter extends FragmentStateAdapter {

    public ViewPage2Adapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                break;
//            case 1:
//                fragment = new FavoriteFragment();
//                break;
            default:
                fragment = new ProfileFragment();

        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
