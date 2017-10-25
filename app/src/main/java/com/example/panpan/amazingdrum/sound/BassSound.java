package com.example.panpan.amazingdrum.sound;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.ArrayMap;

import com.example.panpan.amazingdrum.util.Download;

/**
 * Created by PanXuesen on 2017/10/22.
 */

public class BassSound {
    private SoundPool soundPool;
    private ArrayMap<Integer,Integer> soundId=new ArrayMap<>();
private static float volume=1;

    public void init(Context context,int[] nameList) {
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
                .setMaxStreams(2).build();

        for (int i = 0; i < nameList.length ;i++) {
            soundId.put(nameList[i],soundPool.load(Download.getBassPath2()+"/"+nameList[i]+".ogg",1));
        }
    }
private int previousStreamId=0;
    private int streamId=0;
    public void play(int name) {
        streamId=soundPool.play(soundId.get(name), volume, volume, 1, 0, 1);
        try {
            Thread.sleep(80);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        soundPool.stop(previousStreamId);
        previousStreamId=streamId;
    }

    public static void setVolume(byte volume) {
        BassSound.volume = volume / 100.0f;
    }
    public void release() {
        soundPool.release();
        soundPool = null;
    }
}
