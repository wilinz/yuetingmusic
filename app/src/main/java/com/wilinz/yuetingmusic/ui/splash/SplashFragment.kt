package com.wilinz.yuetingmusic.ui.splash

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.wilinz.yuetingmusic.R
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.wilinz.yuetingmusic.Pref
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {
    private var viewModel: SplashViewModel? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(SplashViewModel::class.java)
        lifecycleScope.launch {
            delay(500)
            val targetFragment =
                if (Pref.getInstance(requireContext()).isFirstLaunch) R.id.action_SplashFragment_to_WelcomeFragment
                else R.id.action_SplashFragment_to_MainFragment
            val navHostFragment = NavHostFragment.findNavController(this@SplashFragment)

            navHostFragment.navigate(targetFragment, null, navOptions {
                popUpTo(R.id.SplashFragment) { this.inclusive = true }
                this.launchSingleTop = true
            })
        }
    }
}