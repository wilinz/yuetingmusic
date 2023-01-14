package com.wilinz.yuetingmusic.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import com.wilinz.yuetingmusic.R
import com.wilinz.yuetingmusic.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private var binding: FragmentMainBinding? = null
    private var viewModel: MainViewModel? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    @SuppressLint("NonConstantResourceId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel!!.playbackStateLiveData.observe(this.viewLifecycleOwner) { state: PlaybackStateCompat ->
            updatePlaybackState(
                state
            )
        }
        viewModel!!.getMediaMetadataLiveData()
            .observe(this.viewLifecycleOwner) { metadata: MediaMetadataCompat? ->
                updateMetadata(metadata)
            }
        viewModel!!.getPlayPositionLiveData()
            .observe(this.viewLifecycleOwner) { position: Long -> updatePosition(position) }
        viewModel!!.getUpdatePictureRotationLiveData()
            .observe(this.viewLifecycleOwner) { value: Float? ->
                binding!!.songAvatar.rotation = value!!
            }
        setViewPage2()
        setBottomNavigation()
        binding!!.songBar.setOnClickListener { v: View? ->
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_MainFragment_to_PlayerFragment)
        }
        binding!!.playPause.setOnClickListener { v: View? ->
            if (viewModel!!.playbackState.state == PlaybackStateCompat.STATE_PAUSED) {
                viewModel!!.play()
            } else if (viewModel!!.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                viewModel!!.pause()
            } else {
                //todo
//                playFromHistory
            }
        }
    }

    private fun setViewPage2() {
        binding!!.viewPage.adapter = ViewPage2Adapter(this)
        binding!!.viewPage.isUserInputEnabled = false
        binding!!.viewPage.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val id = binding!!.bottomNavigation.menu.getItem(position).itemId
                if (binding!!.bottomNavigation.selectedItemId != id) {
                    binding!!.bottomNavigation.selectedItemId = id
                }
            }
        })
    }

    @SuppressLint("NonConstantResourceId")
    private fun setBottomNavigation() {
        binding!!.bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
            val index: Int
            index = when (item.itemId) {
                R.id.page_1 -> 0
                else -> 2
            }
            if (binding!!.viewPage.currentItem != index) {
                binding!!.viewPage.setCurrentItem(index, true)
            }
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun updateMetadata(metadata: MediaMetadataCompat?) {
        Log.d(TAG, "updateMetadata: ")
        if (metadata == null) return
        val description = metadata.description
        Glide.with(requireContext())
            .load(description.iconUri)
            .into(binding!!.songAvatar)
        binding!!.name.text = description.title.toString() + " - " + description.subtitle
    }

    private fun updatePlaybackState(state: PlaybackStateCompat) {
        if (state.state == PlaybackStateCompat.STATE_NONE || state.state == PlaybackStateCompat.STATE_PAUSED) {
            binding!!.playPause.icon =
                ContextCompat.getDrawable(requireContext(), R.drawable.play_arrow_24px)
            viewModel!!.stopPictureRotationTimer()
        } else {
            viewModel!!.startPictureRotationTimer(0.5f)
            binding!!.playPause.icon =
                ContextCompat.getDrawable(requireContext(), R.drawable.round_pause_24)
        }
    }

    private fun updatePosition(position: Long) {
        val mediaMetadata = viewModel!!.getMediaMetadataLiveData().value
        if (mediaMetadata != null) {
            val duration = mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
            val parent = (position / duration.toFloat() * 100 + 0.5).toInt()
            //            Log.d(TAG, "updatePosition: " + position + "/" + duration + ": " + parent + " %");
//            binding.progressIndicator.setSecondaryProgress(100);
//            binding.progressIndicator.setSecondaryProgressTintMode(true);
//            binding.progressIndicator.
            binding!!.progressIndicator.progress = parent.toFloat()
        }
    }

    companion object {
        private const val TAG = "MainFragment"
    }
}