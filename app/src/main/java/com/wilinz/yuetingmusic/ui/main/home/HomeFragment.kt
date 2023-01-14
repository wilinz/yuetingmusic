package com.wilinz.yuetingmusic.ui.main.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle
import com.wilinz.yuetingmusic.data.model.MusicUrl
import com.wilinz.yuetingmusic.data.model.Song
import com.wilinz.yuetingmusic.data.model.TopList
import com.wilinz.yuetingmusic.data.model.TopList.ListBean
import com.wilinz.yuetingmusic.data.model.TopListSong
import com.wilinz.yuetingmusic.data.model.TopListSong.PlaylistBean.TracksBean
import com.wilinz.yuetingmusic.databinding.FragmentHomeBinding
import com.wilinz.yuetingmusic.ui.commen.MusicAdapter
import com.wilinz.yuetingmusic.util.MediaUtil.getSongs
import com.wilinz.yuetingmusic.util.toast
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class HomeFragment : Fragment() {
    private var binding: FragmentHomeBinding? = null
    private var viewModel: HomeViewModel? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewModel()
        setView()
        //        getMusics();
    }

    @SuppressLint("CheckResult")
    private fun setView() {
        val adapter0 = TopListAdapter(java.util.List.of())
        adapter0.setOnGetTracksListener { index: Int ->
            viewModel!!.getTopListDetails(index)
                .compose(
                    AndroidLifecycle.createLifecycleProvider(viewLifecycleOwner).bindToLifecycle()
                )
                .subscribe({ data: TopListSong? -> adapter0.notifyItemChanged(index) }) { e: Throwable ->
                    e.printStackTrace()
                    toast(requireContext(), "获取数据失败：" + e.message)
                }
        }
        adapter0.setOnSongClickListener { index0: Int, index1: Int, songs: List<TracksBean>?, song: TracksBean? ->
            Log.d(TAG, "setView: " + (songs == null))
            viewModel!!.getMusicUrls(songs!!.map { song2: TracksBean -> song2.id })
                .compose(
                    AndroidLifecycle.createLifecycleProvider(viewLifecycleOwner).bindToLifecycle()
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ data: MusicUrl ->
                    val songArrayList = getSongs(
                        songs, data.data!!, true
                    )
                    if (songArrayList == null) {
                        toast(requireContext(), "此歌曲未登录无法播放")
                        return@subscribe
                    }
                    viewModel!!.playFromUri(songArrayList, songArrayList[index1]!!)
                }) { e: Throwable ->
                    e.printStackTrace()
                    toast(requireContext(), "获取数据失败：" + e.message)
                }
        }
        //        binding.topList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
//        binding.topList.setAdapter(adapter0);
        binding!!.viewPage.adapter = adapter0
        val adapter = MusicAdapter(java.util.List.of())
        adapter.setOnItemClickListener { songs: List<Song?>?, index: Int, song: Song? -> }
        /* binding.musicList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.musicList.setAdapter(adapter);*/adapter0.setOnRefresh { v ->
            viewModel!!.topList
                .compose(
                    AndroidLifecycle.createLifecycleProvider(viewLifecycleOwner).bindToLifecycle()
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { s: TopList? -> v.isRefreshing = false }
        }
    }

    private fun setViewModel() {
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        viewModel!!.getEvent().observe(this.viewLifecycleOwner) { event: Event? -> }
        viewModel!!.getSongs().observe(this.viewLifecycleOwner) { songs: List<Song?>? -> }
        viewModel!!.getTopListLiveData().observe(this.viewLifecycleOwner) { topList ->
            if (topList == null) return@observe
            val adapter = (binding!!.viewPage.adapter as TopListAdapter?)!!
            adapter.set(topList.toMutableList())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}