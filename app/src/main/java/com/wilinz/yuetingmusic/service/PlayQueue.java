package com.wilinz.yuetingmusic.service;

import com.wilinz.yuetingmusic.data.model.Song;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class PlayQueue {

    public ArrayList<Song> rawQueue = new ArrayList<>();;
    public ArrayList<Song> queue = new ArrayList<>();

    public void shuffle() {
        Song currentSong = queue.get(currentIndex);
        Collections.copy(rawQueue, queue);
        Collections.shuffle(queue);
        currentIndex = queue.indexOf(currentSong);
    }

    public void restoreShuffle(){
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
        if (currentIndex < queue.size() - 1) {
            currentIndex++;
        } else {
            currentIndex = 0;
        }
        return currentIndex;
    }

    public int moveToPre() {
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
}
