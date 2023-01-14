package com.wilinz.yuetingmusic.ui.recent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.wilinz.yuetingmusic.R
import com.wilinz.yuetingmusic.data.model.Song
import com.wilinz.yuetingmusic.databinding.FragmentPlaylistContainerBinding
import com.wilinz.yuetingmusic.event.RecentRecordUpdatedEvent
import com.wilinz.yuetingmusic.ui.commen.MusicAdapter
import com.wilinz.yuetingmusic.ui.playlist.PlaylistFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RecentListFragment : Fragment() {
    private var viewModel: RecentListViewModel? = null
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refresh(event: RecentRecordUpdatedEvent?) {
        viewModel?.songsList?.subscribe()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)
        viewModel = ViewModelProvider(this).get(RecentListViewModel::class.java)
        val fragment =
            (childFragmentManager.findFragmentById(R.id.fragment_play_list) as PlaylistFragment?)!!
        fragment.binding!!.toolbar.title = "最近播放"
        fragment.binding!!.refresh.setOnRefreshListener { musics }
        viewModel!!.getRefreshingLiveData().observe(viewLifecycleOwner) { isRefreshing: Boolean? ->
            fragment.binding!!.refresh.isRefreshing = isRefreshing ?: false
        }
        viewModel!!.getSongsLiveData().observe(viewLifecycleOwner) { songs: List<Song> ->
            val adapter = fragment.binding!!.playList.adapter as MusicAdapter?
            adapter!!.setSongs(songs)
        }
    }

    val musics: Unit
        get() {
            viewModel?.songsList?.subscribe()
        }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
        binding = null
    }
}