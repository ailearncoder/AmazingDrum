package com.example.panpan.amazingdrum.activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.ArrayMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.panpan.amazingdrum.BleServer;
import com.example.panpan.amazingdrum.R;
import com.example.panpan.amazingdrum.Sound;
import com.example.panpan.amazingdrum.custom.MyUtil;
import com.example.panpan.amazingdrum.sound.ChordPlay;
import com.example.panpan.amazingdrum.util.Download;

import java.io.UnsupportedEncodingException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class RoomActivityBle extends Activity {

    @InjectView(R.id.text_info)
    TextView textInfo;
    @InjectView(R.id.list_members)
    ListView listMembers;
    @InjectView(R.id.button_begin)
    Button buttonBegin;
    private ArrayAdapter<String> adapter;
    private Handler handler = new Handler();
    private Sound sound;
    private BleServer bleServer;
    private ArrayMap<String, String> macName = new ArrayMap<>();
    private ChordPlay chordPlay;
    private final String chords[] = {"G", "D", "Em", "Bm", "Am", "C"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        ButterKnife.inject(this);
        bleServer = new BleServer(this);
        bleServer.setListener(bleListener);
        initView();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        sound = new Sound();
        sound.init(this);
        initSound();
        listRunnable.run();
    }

    private void initSound() {
        chordPlay = new ChordPlay(this);
        chordPlay.setGtpTxtPath(Download.getLocalSongPath() + "/不再犹豫.gtp");
        chordPlay.beginLoadSingleChordFile();
        try {
            chordPlay.loadSingleChordFile(0, "G");
            chordPlay.loadSingleChordFile(0, "D");
            chordPlay.loadSingleChordFile(0, "Em");
            chordPlay.loadSingleChordFile(0, "Bm");
            chordPlay.loadSingleChordFile(0, "Am");
            chordPlay.loadSingleChordFile(0, "C");
        } catch (Exception e) {
            e.printStackTrace();
        }
        chordPlay.endLoadSingleChordFile();
    }

    private void initView() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listMembers.setAdapter(adapter);
        textInfo.setText("How to enter room?\nPlease search:AeroBandRoom");
    }

    @OnClick(R.id.button_begin)
    public void onViewClicked() {
        if (macName.size() > 0) {
            bleServer.sendData((byte) 0x01, (byte) 0x00);
        } else {
            MyUtil.showToast(this, "等待玩家加入房间");
        }
    }

    private boolean isUiRun = false;
    private Runnable listRunnable = new Runnable() {
        @Override
        public void run() {
            adapter.clear();
            if (MainBandActivity.instrumentType == 0)
                adapter.add("房主——吉他");
            if (MainBandActivity.instrumentType == 1)
                adapter.add("房主——架子鼓");
            if (MainBandActivity.instrumentType == 2)
                adapter.add("房主——电吉他");
            if (MainBandActivity.instrumentType == 3)
                adapter.add("房主——贝斯");
            for (int i = 0; i < macName.size(); i++) {
                adapter.add(macName.valueAt(i));
            }
            adapter.notifyDataSetChanged();
            isUiRun = false;
        }
    };
    private BleServer.OnServerListener bleListener = new BleServer.OnServerListener() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    String address = device.getAddress();
                    if (macName.containsKey(address)) {
                        macName.remove(address);
                        runOnUiThread(listRunnable);
                    }
                    break;
            }
        }

        @Override
        public void OnDataReceived(BluetoothDevice device, int offset, byte[] value) {
            switch (value[0]) {
                case 0x00:
                    try {
                        String name = new String(value, 1, value.length - 1, "UTF-8");
                        macName.put(device.getAddress(), name);
                        runOnUiThread(listRunnable);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                case 0x02:
                    if (value[1] == 1) {
                        switch (value[2]) {
                            case 0x01://底鼓
                                sound.play(2);
                                break;
                            case 0x02://军鼓
                                sound.play(3);
                                break;
                            case 0x04://左擦
                                sound.play(0);
                                break;
                            case 0x08://右擦
                                sound.play(1);
                                break;
                            default:
                                break;
                        }
                    } else if (value[1] == 0) {
                        switch (value[2]) {
                            case 0x00:
                                chordPlay.play(chords[value[3]], false);
                                break;
                            case 0x01:
                                chordPlay.play(chords[value[3]], true);
                                break;
                            default:
                                break;
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        bleServer.close();
        sound.release();
        super.onDestroy();
    }
}
