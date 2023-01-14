package com.wilinz.yuetingmusic.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.wilinz.yuetingmusic.Key
import com.wilinz.yuetingmusic.R
import com.wilinz.yuetingmusic.data.model.User
import com.wilinz.yuetingmusic.databinding.FragmentUserListBinding

class UserListFragment : Fragment() {
    private var viewModel: UserListViewModel? = null
    private var binding: FragmentUserListBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserListBinding.inflate(layoutInflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = UserListAdapter(java.util.List.of())
        adapter.setOnItemClickListener { users: List<User?>?, index: Int, user: User ->
            if (user.rememberPassword) {
                viewModel!!.changeActive(user, true).subscribe()
            } else {
                val bundle = Bundle()
                bundle.putParcelable(Key.user, user)
                bundle.putString(Key.username, user.username)
                NavHostFragment.findNavController(this)
                    .navigate(R.id.action_UserListFragment_to_LoginFragment, bundle)
            }
        }
        viewModel = ViewModelProvider(this).get(UserListViewModel::class.java)
        viewModel!!.getUsersLiveDate()
            .observe(viewLifecycleOwner) { users: List<User> -> adapter.setUsers(users) }
        binding!!.userList.layoutManager = LinearLayoutManager(requireContext())
        binding!!.userList.adapter = adapter
        binding!!.refresh.setOnRefreshListener { viewModel!!.allUser.subscribe() }
        binding!!.toolbar.title = "所有用户"
        binding!!.addUser.setOnClickListener { v: View? ->
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_UserListFragment_to_WelcomeFragment)
        }
        //        binding.exitLogin.setOnClickListener(v->{
//            viewModel.exitLogin().subscribe();
//        });
        viewModel!!.getRefreshingLiveData().observe(viewLifecycleOwner) { isRefreshing ->
            binding!!.refresh.isRefreshing = isRefreshing ?: false
        }
    }
}