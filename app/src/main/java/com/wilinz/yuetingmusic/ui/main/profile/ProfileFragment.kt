package com.wilinz.yuetingmusic.ui.main.profile

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle
import com.wilinz.yuetingmusic.R
import com.wilinz.yuetingmusic.data.model.Song
import com.wilinz.yuetingmusic.data.model.User
import com.wilinz.yuetingmusic.databinding.FragmentProfileBinding
import com.wilinz.yuetingmusic.ui.commen.MusicAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null
    private var viewModel: ProfileViewModel? = null
    @SuppressLint("CheckResult")
    var getContentLauncher = registerForActivityResult<String, Uri>(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        viewModel!!.userLiveData.value?.let {
            viewModel!!.setUserAvatar(it, uri)
                .compose(
                    AndroidLifecycle.createLifecycleProvider(
                        viewLifecycleOwner
                    ).bindToLifecycle()
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { user: User? -> },
                    { err: Throwable ->
                        err.printStackTrace()
                        Toast.makeText(requireContext(), "设置头像失败：$err", Toast.LENGTH_SHORT).show()
                    }
                ) { Toast.makeText(requireContext(), "设置头像成功", Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        viewModel!!.userLiveData.observe(viewLifecycleOwner) { user: User? ->
            if (user != null) {
                binding!!.username.text = user.username
                binding!!.nickname.text = user.nickname
                if (user.avatar != null) setAvatarView(Uri.parse(user.avatar))
            }
        }
        val adapter = MusicAdapter(java.util.List.of())
        viewModel!!.getRecentListLiveData().observe(viewLifecycleOwner) { songs: List<Song> ->
            adapter.setSongs(
                songs
            )
        }
        binding!!.user.setOnClickListener { v: View? ->
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_MainFragment_to_UserListFragment)
        }
        binding!!.avatar.setOnClickListener { v: View? -> getContentLauncher.launch("image/*") }
        binding!!.favorite.setOnClickListener { v: View? ->
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_MainFragment_to_FavoriteListFragment)
        }
        binding!!.localMusic.setOnClickListener { v: View? ->
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_MainFragment_to_LocalMusicFragment)
        }
        binding!!.playHistory.setOnClickListener { v: View? ->
            NavHostFragment.findNavController(
                this
            ).navigate(R.id.action_MainFragment_to_RecentListFragment)
        }
        binding!!.settings.setOnClickListener { v: View? ->
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_MainFragment_to_SettingsFragment)
        }
        binding!!.recentPlay.layoutManager = LinearLayoutManager(requireContext())
        adapter.setOnItemClickListener { songs: List<Song?>?, index: Int?, song: Song? ->
            viewModel!!.playFromUri(
                songs,
                song!!
            )
        }
        binding!!.recentPlay.adapter = adapter
    }

    private fun setAvatarView(uri: Uri) {
        try {
            binding!!.avatar.setImageURI(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            binding!!.avatar.setImageResource(R.drawable.avatar2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}