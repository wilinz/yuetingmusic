package com.wilinz.yuetingmusic.ui.player

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wilinz.yuetingmusic.R
import com.wilinz.yuetingmusic.constant.PlayMode
import com.wilinz.yuetingmusic.databinding.FragmentPlayerBinding
import com.wilinz.yuetingmusic.util.LogUtil.d
import com.wilinz.yuetingmusic.util.ScreenUtil.getNavigationBarHeight
import com.wilinz.yuetingmusic.util.TimeUtil.format
import com.wilinz.yuetingmusic.util.UriUtil.idToUri
import jp.wasabeef.glide.transformations.BlurTransformation

class PlayerFragment : Fragment() {
    private var viewModel: PlayerViewModel? = null
    private var binding: FragmentPlayerBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBottomPadding()
        viewModel = ViewModelProvider(this).get(PlayerViewModel::class.java)
        // Create MediaBrowserServiceCompat
        viewModel!!.getPlayPositionLiveData()
            .observe(this.viewLifecycleOwner) { position: Long -> updatePosition(position) }
        viewModel!!.playbackStateLiveData.observe(this.viewLifecycleOwner) { state: PlaybackStateCompat ->
            updatePlaybackState(
                state
            )
        }
        viewModel!!.getMediaMetadataLiveData()
            .observe(this.viewLifecycleOwner) { metadata: MediaMetadataCompat? ->
                updateMetadata(metadata)
            }
        viewModel!!.getPlayModeLiveData()
            .observe(this.viewLifecycleOwner) { playMode: Int -> updatePlayMode(playMode) }
        viewModel!!.getUpdatePictureRotationLiveData()
            .observe(this.viewLifecycleOwner) { value: Float? ->
                binding!!.avatar.rotation = value!!
            }
        binding!!.playPause.setOnClickListener { v: View? ->
            val pbState = viewModel!!.playbackState.state
            d(TAG, Integer.valueOf(pbState).toString())
            if (pbState == PlaybackStateCompat.STATE_PAUSED) {
                viewModel!!.play()
            } else if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                viewModel!!.pause()
            }
        }
        binding!!.skipToPrevious.setOnClickListener { v: View? -> viewModel!!.skipToPrevious() }
        binding!!.skipToNext.setOnClickListener { v: View? -> viewModel!!.skipToNext() }
        //        binding.currentProgress.setLabelFormatter((value) -> TimeUtil.format((long) value));
//        binding.currentProgress.addOnChangeListener((slider, value, fromUser) -> {
//            if (fromUser) viewModel.seekTo((long) value);
//        });
        binding!!.currentProgress.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                viewModel!!.seekTo(seekBar.progress.toLong())
            }
        })
        binding!!.switchPlayMode.setOnClickListener { v: View? -> viewModel!!.switchPlayMode() }
        binding!!.favorite.setOnClickListener { v: View? -> viewModel!!.saveFavoriteSong() }
    }

    private fun setBottomPadding() {
        val layoutParams = binding!!.bottomPadding.layoutParams
        layoutParams.height = getNavigationBarHeight(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun updatePlayMode(playMode: Int) {
        var resId = 0
        resId = if (playMode == PlayMode.ORDERLY) {
            R.drawable.round_repeat_24
        } else if (playMode == PlayMode.SINGLE_LOOP) {
            R.drawable.round_repeat_one_24
        } else {
            R.drawable.round_shuffle_24
        }
        binding!!.switchPlayMode.icon = ContextCompat.getDrawable(requireContext(), resId)
    }

    private fun updatePlaybackState(state: PlaybackStateCompat) {
        if (state.state == PlaybackStateCompat.STATE_NONE || state.state == PlaybackStateCompat.STATE_PAUSED) {
            binding!!.playPause.icon =
                ContextCompat.getDrawable(requireContext(), R.drawable.play_arrow_24px)
            viewModel!!.stopPictureRotationTimer()
        } else {
            viewModel!!.startPictureRotationTimer(0.25f)
            binding!!.playPause.icon =
                ContextCompat.getDrawable(requireContext(), R.drawable.round_pause_24)
        }
    }

    private fun updatePosition(position: Long) {
        if (position <= binding!!.currentProgress.max) {
            try {
                binding!!.currentProgress.progress = position.toInt()
                binding!!.currentProgressTime.text = format(position)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateMetadata(metadata: MediaMetadataCompat?) {
        if (metadata == null) return
        val description = metadata.description
        binding!!.name.text = description.title.toString() + " - " + description.subtitle
        if (description.iconUri != idToUri(requireContext(), R.drawable.icon)) {
            Glide.with(requireContext())
                .load(description.iconUri)
                .apply(RequestOptions.bitmapTransform(BlurTransformation(50, 3)))
                .into(binding!!.backgroundImage)
        }

        /*    Glide.with(requireContext())
                .asBitmap()
                .load(description.getIconUri())
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 3)))
                .into(new CustomTarget<Bitmap>(binding.backgroundImage.getWidth(), binding.backgroundImage.getHeight()) {
            @Override
            public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                Palette.from(bitmap)
                        .
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });*/Glide.with(requireContext())
            .load(description.iconUri)
            .error(R.drawable.logo)
            .into(binding!!.avatar)
        val duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
        Log.d(TAG, "updateMetadata: " + duration + ", " + format(duration))
        if (duration > 0) {
            Log.d(TAG, "updateMetadata2: " + duration + ", " + format(duration))
            //            binding.currentProgress.setProgress(0);
            binding!!.currentProgress.max = duration.toInt()
            binding!!.duration.text = format(duration)
        }
    }

    companion object {
        private const val TAG = "PlayerFragment"
    }
}