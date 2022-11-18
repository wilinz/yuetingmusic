package com.wilinz.yuetingmusic.service;

import com.wilinz.yuetingmusic.data.model.Song;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PlayQueue {

    public ArrayList<Song> shuffledQueue = new ArrayList<>();

    public ArrayList<Song> queue = new ArrayList<>();

    public List<Song> getQueue() {
        if (isShuffleMode()) return shuffledQueue;
        else return queue;
    }

    public void setQueue(List<Song> songs) {
        Collections.copy(queue, songs);
        Collections.copy(shuffledQueue, songs);
        Collections.shuffle(shuffledQueue);
    }

    private boolean isShuffleMode = false;
    private boolean isShuffled = false;

    private void shuffle() {
        Song currentSong = queue.get(currentIndex);
        Collections.copy(rawQueue, queue);
        Collections.shuffle(queue);
        currentIndex = queue.indexOf(currentSong);
    }

    public void restoreShuffle() {
        Song currentSong = queue.get(currentIndex);
        Collections.copy(queue, rawQueue);
        currentIndex = queue.indexOf(currentSong);
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public boolean setCurrentIndex(int currentIndex) {
        if (currentIndex < queue.size() && currentIndex >= 0) {
            this.currentIndex = currentIndex;
            return true;
        }
        return false;
    }

    public int moveToNext() {
        handleShuffle();
        if (currentIndex < queue.size() - 1) {
            currentIndex++;
        } else {
            currentIndex = 0;
        }
        return currentIndex;
    }

    private void handleShuffle() {
        if (isShuffleMode() && !isShuffled) {
            shuffle();
            isShuffled = true;
        } else if (!isShuffleMode() && isShuffled) {
            restoreShuffle();
            isShuffled = false;
        }
    }

    public int moveToPre() {
        handleShuffle();
        if (currentIndex > 0) {
            currentIndex--;
        } else {
            currentIndex = queue.size() - 1;
        }
        return currentIndex;
    }

    private int currentIndex = 0;

    public Song getSong() {
        return queue.get(currentIndex);
    }

    public boolean isShuffleMode() {
        return isShuffleMode;
    }

    public void setShuffleMode(boolean shuffleMode) {
        isShuffleMode = shuffleMode;
        shuffle();
    }
}
