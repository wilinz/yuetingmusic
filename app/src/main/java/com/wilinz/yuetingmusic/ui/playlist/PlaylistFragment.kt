package com.wilinz.yuetingmusic.ui.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wilinz.yuetingmusic.R
import com.wilinz.yuetingmusic.data.model.Song
import com.wilinz.yuetingmusic.databinding.FragmentPlaylistBinding
import com.wilinz.yuetingmusic.ui.commen.MusicAdapter

class PlaylistFragment : Fragment {
    private var viewModel: PlaylistViewModel? = null
    lateinit var binding: FragmentPlaylistBinding
    private var songs: List<Song>? = null

    constructor() {}
    constructor(songs: List<Song>?) {
        this.songs = songs
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlaylistBinding.inflate(layoutInflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(PlaylistViewModel::class.java)
        val adapter = MusicAdapter(java.util.List.of())
        adapter.setOnItemClickListener { songs: List<Song?>?, index: Int?, song: Song? ->
            viewModel!!.playFromUri(
                songs,
                song!!
            )
        }
        binding!!.playList.layoutManager = LinearLayoutManager(requireContext())
        binding!!.playList.adapter = adapter
        binding!!.refresh.setColorSchemeResources(R.color.my_primary)
    }
}