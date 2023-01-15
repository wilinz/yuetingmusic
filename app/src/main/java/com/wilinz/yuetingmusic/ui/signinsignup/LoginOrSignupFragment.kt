package com.wilinz.yuetingmusic.ui.signinsignup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.wilinz.yuetingmusic.Key
import com.wilinz.yuetingmusic.Pref.Companion.getInstance
import com.wilinz.yuetingmusic.R
import com.wilinz.yuetingmusic.data.AppNewWork
import com.wilinz.yuetingmusic.data.model.User
import com.wilinz.yuetingmusic.databinding.FragmentLoginBinding
import com.wilinz.yuetingmusic.util.toast
import kotlinx.coroutines.launch

class LoginOrSignupFragment : Fragment() {
    private var binding: FragmentLoginBinding? = null
    private var isLoginMode = false
    private var viewModel: LoginOrSignupViewModel? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginOrSignupViewModel::class.java)
        viewModel!!.signupResult.observe(viewLifecycleOwner) { success: Boolean ->
            if (success) {
                NavHostFragment.findNavController(this)
                    .navigate(R.id.action_LoginFragment_to_MainFragment)
            }
        }
        val bundle =
            NavHostFragment.findNavController(this).currentBackStackEntry!!.arguments ?: return
        val user = bundle.getParcelable<User>(Key.user)
        val username = bundle.getString(Key.username)
        isLoginMode = /*user != null*/true
        lifecycleScope.launch {
            AppNewWork.loginService.sendCaptcha(username!!)
        }

        val textResId = if (isLoginMode) R.string.login else R.string.signup
        binding!!.loginOrSignup.setText(textResId)
        binding!!.loginOrSignupLabel.setText(textResId)
        binding!!.forgetPassword.visibility = if (isLoginMode) View.VISIBLE else View.GONE
        binding!!.loginOrSignup.setOnClickListener { v: View? ->
            val password = binding!!.password.editText!!
                .text.toString()
//            if (!isLoginMode && password.length < 6) {
//                toast(requireContext(), "密码长度必须大于或等于6位")
//                return@setOnClickListener
//            }

            if (isLoginMode) {

                lifecycleScope.launch {
                    AppNewWork.loginService.loginByCaptcha(
                        username!!,
                        password
                    )
                }
                return@setOnClickListener
                if (viewModel!!.login(user!!, password, binding!!.rememberPassword.isChecked)) {
                    toast(requireContext(), "登录成功")
                    NavHostFragment.findNavController(this)
                        .navigate(R.id.action_LoginFragment_to_MainFragment)
                    getInstance(requireContext())!!.isFirstLaunch = false
                } else {
                    toast(requireContext(), "登录失败：密码错误")
                }
            } else {
                viewModel!!.signup(username, password, binding!!.rememberPassword.isChecked)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}