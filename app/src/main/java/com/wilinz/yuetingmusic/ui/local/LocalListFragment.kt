package com.wilinz.yuetingmusic.ui.local

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.permissionx.guolindev.PermissionX
import com.wilinz.yuetingmusic.R
import com.wilinz.yuetingmusic.data.model.Song
import com.wilinz.yuetingmusic.databinding.FragmentPlaylistContainerBinding
import com.wilinz.yuetingmusic.ui.commen.MusicAdapter
import com.wilinz.yuetingmusic.ui.playlist.PlaylistFragment

class LocalListFragment : Fragment() {
    private var viewModel: LocalListViewModel? = null
    private var binding: FragmentPlaylistContainerBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlaylistContainerBinding.inflate(
            layoutInflater, container, false
        )
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(LocalListViewModel::class.java)
        val fragment =
            (childFragmentManager.findFragmentById(R.id.fragment_play_list) as PlaylistFragment?)!!
        fragment.binding.toolbar.title = "本地音乐"
        fragment.binding.refresh.setOnRefreshListener { musics }
        viewModel!!.refreshingLiveData.observe(viewLifecycleOwner) { isRefreshing: Boolean? ->
            fragment.binding.refresh.isRefreshing = isRefreshing ?: false
        }
        viewModel!!.songsLiveData.observe(viewLifecycleOwner) { songs: List<Song> ->
            val adapter = fragment.binding.playList.adapter as MusicAdapter?
            adapter!!.setSongs(songs)
        }
        if (viewModel!!.songsLiveData.value == null) musics
    }

    val musics: Unit
        get() {
            PermissionX.init(requireActivity())
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .request { allGranted: Boolean, grantedList: List<String?>?, deniedList: List<String?>? ->
                    if (allGranted) {
                        viewModel!!.songsList.subscribe()
                    } else {
                        Toast.makeText(requireContext(), "这些权限被拒绝: \$deniedList", Toast.LENGTH_LONG)
                            .show()
                    }
                }
        }
}