package com.wilinz.yuetingmusic.service.media;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;

public class A {
//    private Context context;
//    MediaSessionCompat.Callback callback = new
//            MediaSessionCompat.Callback() {
//                @Override
//                public void onPlay() {
//                    AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//                    // Request audio focus for playback, this registers the afChangeListener
//                    AudioAttributes attrs = new AudioAttributes.Builder()
//                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                            .build();
//                    AudioFocusRequest audioFocusRequest = null;
//                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                        audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
//                                .setOnAudioFocusChangeListener(afChangeListener)
//                                .setAudioAttributes(attrs)
//                                .build();
//                    }
//                    int result = am.requestAudioFocus(audioFocusRequest);
//
//                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//                        // Start the service
//                        startService(new Intent(context, MediaBrowserService.class));
//                        // Set the session active  (and update metadata and state)
//                        mediaSession.setActive(true);
//                        // start the player (custom call)
//                        player.start();
//                        // Register BECOME_NOISY BroadcastReceiver
//                        registerReceiver(myNoisyAudioStreamReceiver, intentFilter);
//                        // Put the service in the foreground, post notification
//                        service.startForeground(id, myPlayerNotification);
//                    }
//                }
//
//                @Override
//                public void onStop() {
//                    AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//                    // Abandon audio focus
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        am.abandonAudioFocusRequest(audioFocusRequest);
//                    }
//                    unregisterReceiver(myNoisyAudioStreamReceiver);
//                    // Stop the service
//                    service.stopSelf();
//                    // Set the session inactive  (and update metadata and state)
//                    mediaSession.setActive(false);
//                    // stop the player (custom call)
//                    player.stop();
//                    // Take the service out of the foreground
//                    service.stopForeground(false);
//                }
//
//                @Override
//                public void onPause() {
//                    AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//                    // Update metadata and state
//                    // pause the player (custom call)
//                    player.pause();
//                    // unregister BECOME_NOISY BroadcastReceiver
//                    unregisterReceiver(myNoisyAudioStreamReceiver);
//                    // Take the service out of the foreground, retain the notification
//                    service.stopForeground(false);
//                }
//            };

}
