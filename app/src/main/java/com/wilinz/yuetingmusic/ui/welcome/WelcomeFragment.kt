package com.wilinz.yuetingmusic.ui.welcome

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle
import com.wilinz.yuetingmusic.Key
import com.wilinz.yuetingmusic.Pref.Companion.getInstance
import com.wilinz.yuetingmusic.R
import com.wilinz.yuetingmusic.data.model.User
import com.wilinz.yuetingmusic.databinding.FragmentWelcomeBinding
import com.wilinz.yuetingmusic.util.toast
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.*

class WelcomeFragment : Fragment() {
    private var binding: FragmentWelcomeBinding? = null
    private var viewModel: WelcomeViewModel? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(WelcomeViewModel::class.java)
        viewModel!!.signupResult.observe(viewLifecycleOwner) { success: Boolean ->
            if (success) {
                val navController = NavHostFragment.findNavController(this)
                if (navController.previousBackStackEntry == null) {
                    navController.navigate(R.id.action_FirstFragment_to_MainFragment)
                } else {
                    navController.popBackStack()
                }
                getInstance(requireContext())!!.isFirstLaunch = false
            }
        }
        binding!!.continue1.setOnClickListener { v: View? ->
            val username = binding!!.username.editText!!
                .text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(username)) {
                toast(requireContext(), "用户名不能为空")
                return@setOnClickListener
            }
            viewModel!!.getUser(username)
                .observeOn(AndroidSchedulers.mainThread())
                .compose<Optional<User>>(
                    AndroidLifecycle.createLifecycleProvider(viewLifecycleOwner).bindToLifecycle()
                )
                .subscribe(
                    { user: Optional<User> ->
                        val bundle = Bundle()
                        bundle.putParcelable(Key.user, user.orElse(null))
                        bundle.putString(
                            Key.username,
                            binding!!.username.editText!!.text.toString()
                        )
                        NavHostFragment.findNavController(this)
                            .navigate(R.id.action_WelcomeFragment_to_LoginFragment, bundle)
                    }
                ) { err: Throwable ->
                    err.printStackTrace()
                    Toast.makeText(requireContext(), "登录或注册失败：$err", Toast.LENGTH_LONG).show()
                }
        }
        binding!!.notLoggedIn.setOnClickListener { v: View? ->
            val navController = NavHostFragment.findNavController(this)
            if (navController.previousBackStackEntry == null) {
                viewModel!!.signupVisitor()
            } else {
                navController.popBackStack()
            }
            getInstance(requireContext())!!.isFirstLaunch = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}