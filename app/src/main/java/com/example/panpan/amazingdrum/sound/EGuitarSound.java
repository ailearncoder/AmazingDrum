package com.example.panpan.amazingdrum.sound;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.ArrayMap;

import com.example.panpan.amazingdrum.util.Download;
import com.example.panpan.amazingdrum.util.GuitarSound2Piano;

/**
 * Created by PanXuesen on 2017/10/22.
 */

public class EGuitarSound {
    private SoundPool soundPool;
    private ArrayMap<Integer, Integer> soundId = new ArrayMap<>();
    private static float volume=1;
    public void init(Context context, int[] nameList) {
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
                .setMaxStreams(6).build();

        for (int i = 0; i < nameList.length; i++) {
            soundId.put(nameList[i], soundPool.load(Download.getElectricGuitarPath() + "/" + GuitarSound2Piano.getElectricGuitarName(nameList[i]), 1));
        }
    }

    private int streamId[] = new int[6];
    private int streamId2[] = new int[6];

    public void play(int[] name) {
        for (int i = 0; i < name.length; i++) {
            streamId[i] = soundPool.play(soundId.get(name[i]), volume, volume, 1, 0, 1);
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < streamId2.length; i++) {
            soundPool.stop(streamId2[i]);
        }
        System.arraycopy(streamId, 0, streamId2, 0, streamId.length);
    }

    //静音
    public void silence() {
        for (int i = 0; i < streamId.length; i++) {
            soundPool.stop(streamId[i]);
        }
    }
    public static void setVolume(byte volume) {
        EGuitarSound.volume = volume / 100.0f;
    }
    public void release() {
        soundPool.release();
        soundPool = null;
    }
}
