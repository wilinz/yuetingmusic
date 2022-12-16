package com.wilinz.yuetingmusic.ui.main.home;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.permissionx.guolindev.PermissionX;
import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle;
import com.wilinz.yuetingmusic.data.model.Song;
import com.wilinz.yuetingmusic.databinding.FragmentHomeBinding;
import com.wilinz.yuetingmusic.ui.commen.MusicAdapter;
import com.wilinz.yuetingmusic.util.MediaUtil;
import com.wilinz.yuetingmusic.util.ToastUtilKt;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import kotlin.collections.CollectionsKt;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;


    private static String TAG = "HomeFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setViewModel();
        setView();
//        getMusics();
    }

    private void setView() {
        TopListAdapter adapter0 = new TopListAdapter(List.of());
        adapter0.setOnGetTracksListener((index) -> {
            viewModel.getTopListDetails(index)
                    .compose(AndroidLifecycle.createLifecycleProvider(getViewLifecycleOwner()).bindToLifecycle())
                    .subscribe((data) -> {
                        adapter0.notifyItemChanged(index);
                    }, e -> {
                        e.printStackTrace();
                        ToastUtilKt.toast(requireContext(), "获取数据失败：" + e.getMessage());
                    });
        });
        adapter0.setOnSongClickListener(((index0, index1, songs, song) -> {

            Log.d(TAG, "setView: " + (songs == null));
            viewModel.getMusicUrls(CollectionsKt.map(songs, song2 -> song2.id))
                    .compose(AndroidLifecycle.createLifecycleProvider(getViewLifecycleOwner()).bindToLifecycle())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(data -> {
                        List<Song> songArrayList = MediaUtil.getSongs(songs, data.data, true);
                        if (songArrayList == null) {
                            ToastUtilKt.toast(requireContext(), "此歌曲未登录无法播放");
                            return;
                        }
                        viewModel.playFromUri(songArrayList, songArrayList.get(index1));
                    }, e -> {
                        e.printStackTrace();
                        ToastUtilKt.toast(requireContext(), "获取数据失败：" + e.getMessage());
                    });

        }));
//        binding.topList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
//        binding.topList.setAdapter(adapter0);

        binding.viewPage.setAdapter(adapter0);
        MusicAdapter adapter = new MusicAdapter(List.of());
        adapter.setOnItemClickListener((songs, index, song) -> {
//            MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(requireActivity());
//            Bundle bundle = new Bundle();
//            bundle.putParcelableArrayList(Key.songList, (ArrayList<? extends Parcelable>) songs);
//            mediaController.getTransportControls().playFromUri(song.uri,bundle);
        });
       /* binding.musicList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.musicList.setAdapter(adapter);*/

        adapter0.setOnRefresh(v -> {
            viewModel.getTopList()
                    .compose(AndroidLifecycle.createLifecycleProvider(getViewLifecycleOwner()).bindToLifecycle())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> v.setRefreshing(false));
        });
    }

    private void setViewModel() {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        viewModel.getEvent().observe(this.getViewLifecycleOwner(), (event) -> {
        /*    if (event == Event.GetMusicsSuccess && binding.swipeRefresh.isRefreshing()) {
                binding.swipeRefresh.setRefreshing(false);
            }*/
        });
        viewModel.getSongs().observe(this.getViewLifecycleOwner(), songs -> {
          /*  MusicAdapter adapter = (MusicAdapter) binding.musicList.getAdapter();
            LogUtil.d(TAG, songs.toString());
            assert adapter != null;
            adapter.setSongs(songs);*/
        });

        viewModel.getTopListLiveData().observe(this.getViewLifecycleOwner(), topList -> {
            if (topList == null) return;
            TopListAdapter adapter = (TopListAdapter) binding.viewPage.getAdapter();
            assert adapter != null;
            adapter.set(topList);
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
