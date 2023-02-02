package com.wilinz.yuetingmusic.ui.player

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
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
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chayangkoon.champ.glide.ktx.intoCustomTarget
import com.wilinz.yuetingmusic.R
import com.wilinz.yuetingmusic.constant.PlayMode
import com.wilinz.yuetingmusic.databinding.FragmentPlayerBinding
import com.wilinz.yuetingmusic.util.*
import com.wilinz.yuetingmusic.util.LogUtil.d
import com.wilinz.yuetingmusic.util.ScreenUtil.getNavigationBarHeight
import com.wilinz.yuetingmusic.util.TimeUtil.format
import com.wilinz.yuetingmusic.util.UriUtil.idToUri
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlin.math.max

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
        val size = ScreenUtil.dpToPx(binding!!.avatar.context, 300)
        val imageUrl = description.iconUri.toString() + "?param=${size}y${size}"

        Glide.with(requireContext())
            .asBitmap()
            .load(imageUrl)
            .placeholder(binding!!.avatar.drawable)
            .error(R.drawable.logo)
            .intoCustomTarget({ resource, _ ->
                setBackgroundImage(description, resource)
                setAvatar(resource, size)
            })

        val duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
        Log.d(TAG, "updateMetadata: " + duration + ", " + format(duration))
        if (duration > 0) {
            Log.d(TAG, "updateMetadata2: " + duration + ", " + format(duration))
            //            binding.currentProgress.setProgress(0);
            binding!!.currentProgress.max = duration.toInt()
            binding!!.duration.text = format(duration)
        }
    }

    private fun setAvatar(resource: Bitmap?, size: Int) {
        Glide.with(requireContext()).load(resource).override(size, size)
            .into(binding!!.avatar)
    }

    private fun setBackgroundImage(
        description: MediaDescriptionCompat,
        resource: Bitmap
    ) {
        val width = binding!!.backgroundImage.width
        val height = binding!!.backgroundImage.height
        val backgroundImage =
            if (description.iconUri != idToUri(requireContext(), R.drawable.icon)) {
                zoomImg(
                    resource,
                    width,
                    height
                )
            } else {
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(0xfff)
                bitmap
            }
        Glide.with(requireContext())
            .asBitmap()
            .load(backgroundImage)
            .override(width, height)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(50, 3)))
            .intoCustomTarget({ bitmap0, _ ->
                binding!!.backgroundImage.setImageBitmap(bitmap0)
                val bitmap = zoomImg(
                    bitmap0,
                    width,
                    height
                )
                setStatusBarTint(bitmap)
                setBottomViewTint(bitmap)
            })
    }

    private fun setBottomViewTint(bitmap: Bitmap) {
        binding!!.bottom.getBackgroundTint(bitmap) { isDark ->
            binding!!.currentProgress.progressBackgroundTintList =
                ColorStateList.valueOf(if (isDark) Color.WHITE else Color.BLACK)

            listOf(
                binding!!.name,
                binding!!.currentProgressTime,
                binding!!.duration
            ).forEach { view ->
                view.setTextColor(if (isDark) Color.WHITE else Color.BLACK)
            }

            listOf(
                binding!!.favorite,
                binding!!.switchPlayMode,
                binding!!.skipToPrevious,
                binding!!.playPause,
                binding!!.skipToNext,
                binding!!.playList
            ).forEach { view ->
                view.iconTint =
                    ColorStateList.valueOf(if (isDark) Color.WHITE else Color.BLACK)
            }
        }
    }

    private fun setStatusBarTint(resource: Bitmap) {
        val statusBarHeight =
            ScreenUtil.getStatusBarHeight(this@PlayerFragment.requireContext())
        Palette
            .from(resource)
            .maximumColorCount(24)
            .setRegion(
                0,
                0,
                resource.width,
                statusBarHeight
            )
            .generate {
                val isDark = it?.isDark() ?: resource.isDark(
                    0,
                    0,
                    resource.width,
                    statusBarHeight
                )
                setStatusBarTint(
                    requireActivity().window,
                    !isDark
                )
            }
    }

    private fun View.getBackgroundTint(background: Bitmap, callback: (isDark: Boolean) -> Unit) {
        val view = this
        val rect = view.getBoundsByRoot(binding!!.root)

        Log.d(TAG, "getBackgroundTint: ${background.width}, ${background.height}")
        Log.d(TAG, "getBackgroundTint: ${rect}")
        Palette
            .from(background)
            .maximumColorCount(24)
            .setRegion(rect.left, rect.top, rect.right, rect.bottom)
            .generate {
                val isDark = it?.isDark() ?: background.isDark(
                    rect.left, rect.top, rect.right, rect.bottom
                )
                callback(isDark)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        val currentNightMode =
            requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val window = requireActivity().window
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                setStatusBarTint(window, true)
            } // Night mode is not active, we're using the light theme
            Configuration.UI_MODE_NIGHT_YES -> {
                setStatusBarTint(window, false)
            } // Night mode is active, we're using dark theme
        }
    }

    companion object {
        private const val TAG = "PlayerFragment"
    }
}