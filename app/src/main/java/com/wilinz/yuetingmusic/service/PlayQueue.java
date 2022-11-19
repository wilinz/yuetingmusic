package com.wilinz.yuetingmusic.service;

import android.net.Uri;

import androidx.core.graphics.drawable.IconKt;

import com.wilinz.yuetingmusic.data.model.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import kotlin.ResultKt;
import kotlin.collections.CollectionsKt;

public class PlayQueue {

    private volatile static PlayQueue instance = null;

    private PlayQueue() {
    }

    public static PlayQueue getInstance() {
        if (instance == null) {
            synchronized (PlayQueue.class) {
                if (instance == null) {
                    instance = new PlayQueue();
                }
            }
        }
        return instance;
    }

    private ArrayList<Song> shuffledQueue = new ArrayList<>();

    private ArrayList<Song> queue = new ArrayList<>();

    public List<Song> getQueue() {
        if (isShuffleMode()) return shuffledQueue;
        return queue;
    }

    public List<Song> getRawQueue() {
        return queue;
    }

    public void set(List<Song> songs) {
        synchronized (this) {
            queue.clear();
            queue.addAll(songs);
            shuffledQueue.clear();
            shuffledQueue.addAll(songs);
            Collections.shuffle(shuffledQueue);
        }
    }

    public Song getSong() {
        synchronized (this) {
            if (currentIndex>0) return getQueue().get(currentIndex);
            return null;
        }
    }

    public int setCurrentIndexByUri(Uri uri) {
        synchronized (this) {
            int index = CollectionsKt.indexOfFirst(getQueue(), (song -> song.uri.equals(uri)));
            if (index >= 0) currentIndex = index;
            return index;
        }
    }

    public void add(Song song) {
        synchronized (this) {
            queue.add(song);
            Random r = new Random();
            int randomIndex = r.nextInt(shuffledQueue.size());
            Song currentSong = getSong();
            shuffledQueue.add(randomIndex, song);
            if (isShuffleMode) {
                currentIndex = shuffledQueue.indexOf(currentSong);
            }
        }
    }

    public void addAll(List<Song> songs) {
        for (Song song : songs) {
            add(song);
        }
    }

    private boolean isShuffleMode = false;

    public int getCurrentIndex() {
        return currentIndex;
    }

    private boolean setCurrentIndex(int currentIndex) {
        if (currentIndex < getQueue().size() && currentIndex >= 0) {
            this.currentIndex = currentIndex;
            return true;
        }
        return false;
    }

    public int moveToNext() {
        synchronized (this) {
            if (currentIndex < getQueue().size() - 1) {
                currentIndex++;
            } else {
                currentIndex = 0;
            }
            return currentIndex;
        }
    }

    public int moveToPrevious() {
        synchronized (this) {
            if (currentIndex > 0) {
                currentIndex--;
            } else {
                currentIndex = getQueue().size() - 1;
            }
            return currentIndex;
        }
    }

    private int currentIndex = 0;


    public boolean isShuffleMode() {
        return isShuffleMode;
    }

    public void setShuffleMode(boolean shuffleMode) {
        isShuffleMode = shuffleMode;
    }

}
