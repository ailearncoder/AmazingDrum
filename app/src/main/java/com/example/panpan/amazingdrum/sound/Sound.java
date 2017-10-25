package com.example.panpan.amazingdrum.sound;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;

import com.example.panpan.amazingdrum.R;

/**
 * Created by PanXuesen on 2017/8/22.
 */
@SuppressWarnings("ResourceType")
public class Sound {
    private SoundPool soundPool;
    private int soundId[] = new int[4];
    private static float volume=1;
    public void init(Context context) {
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(
                        new AudioAttributes
                                .Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
                                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                                .build()
                )
                .setMaxStreams(4).build();
        soundId[0] = soundPool.load(context, R.raw.c, 1);
        soundId[1] = soundPool.load(context, R.raw.d, 1);
        soundId[2] = soundPool.load(context, R.raw.a, 1);
        soundId[3] = soundPool.load(context, R.raw.b, 1);
    }

    public void play(int index) {
        soundPool.play(soundId[index], volume, volume, 1, 0, 1);
    }
    public static void setVolume(byte volume) {
        Sound.volume = volume / 100.0f;
    }
    public void release() {
        soundPool.release();
        soundPool = null;
    }
}

