package com.wilinz.yuetingmusic.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.wilinz.yuetingmusic.R
import com.wilinz.yuetingmusic.data.model.Song
import com.wilinz.yuetingmusic.databinding.FragmentPlaylistContainerBinding
import com.wilinz.yuetingmusic.event.FavoriteUpdatedEvent
import com.wilinz.yuetingmusic.ui.commen.MusicAdapter
import com.wilinz.yuetingmusic.ui.playlist.PlaylistFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FavoriteListFragment : Fragment() {
    private var viewModel: FavoriteListViewModel? = null
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
        EventBus.getDefault().register(this)
        viewModel = ViewModelProvider(this).get(FavoriteListViewModel::class.java)
        val fragment =
            (childFragmentManager.findFragmentById(R.id.fragment_play_list) as PlaylistFragment?)!!
        fragment.binding.toolbar.title = "我的收藏"
        fragment.binding.refresh.setOnRefreshListener {
            viewModel!!.songsList
                .subscribe()
        }
        viewModel!!.refreshingLiveData.observe(viewLifecycleOwner) { isRefreshing: Boolean? ->
            fragment.binding.refresh.isRefreshing = isRefreshing ?: false
        }
        viewModel!!.songsLiveData.observe(viewLifecycleOwner) { songs: List<Song> ->
            val adapter = fragment.binding.playList.adapter as MusicAdapter?
            adapter!!.setSongs(songs)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refresh(event: FavoriteUpdatedEvent?) {
        viewModel!!.songsList.subscribe()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
        binding = null
    }
}