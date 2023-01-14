package com.wilinz.yuetingmusic.ui.main

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.wilinz.yuetingmusic.ui.main.home.HomeFragment
import com.wilinz.yuetingmusic.ui.main.profile.ProfileFragment

class ViewPage2Adapter(fragment: Fragment?) : FragmentStateAdapter(
    fragment!!
) {
    override fun createFragment(position: Int): Fragment {
        val fragment: Fragment
        fragment = when (position) {
            0 -> HomeFragment()
            else -> ProfileFragment()
        }
        return fragment
    }

    override fun getItemCount(): Int {
        return 2
    }
}