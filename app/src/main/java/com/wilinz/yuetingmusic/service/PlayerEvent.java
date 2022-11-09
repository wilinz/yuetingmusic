package com.wilinz.yuetingmusic.service;

import android.media.MediaPlayer;
import android.util.Log;

import androidx.annotation.NonNull;

import com.wilinz.yuetingmusic.data.model.Song;

public class PlayerEvent {
    public static class PlayEvent {

        public Song audio;

        public PlayEvent(Song song) {
            this.audio = song;
        }
    }

    public static class PauseEvent {
    }

    public static class ProgressChangeEvent {
        @NonNull
        @Override
        public String toString() {
            return "ProgressChangeEvent{" +
                    "progress=" + progress +
                    ", duration=" + duration +
                    '}';
        }

        public long progress;
        public long duration;

        public ProgressChangeEvent(long progress, long duration) {
            this.progress = progress;
            this.duration = duration;
        }
    }

    public static class SeekEvent {
        long position;

        public SeekEvent(long position) {
            this.position = position;
        }
    }

    public static class CreateMediaPlayerEvent {

        public MediaPlayer player;

        public CreateMediaPlayerEvent(MediaPlayer player) {
            this.player = player;
        }
    }
}
