package com.example.panpan.amazingdrum.activity;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.panpan.amazingdrum.BleLink;
import com.example.panpan.amazingdrum.R;
import com.example.panpan.amazingdrum.ServerThread;
import com.example.panpan.amazingdrum.custom.MyUtil;
import com.example.panpan.amazingdrum.sound.BassSound;
import com.example.panpan.amazingdrum.sound.ChordPlay;
import com.example.panpan.amazingdrum.sound.EGuitarSound;
import com.example.panpan.amazingdrum.sound.Sound;
import com.example.panpan.amazingdrum.util.Download;
import com.example.panpan.amazingdrum.util.IpAdressUtils;
import com.example.panpan.amazingdrum.util.UartDataDeal;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class RoomActivity2 extends Activity {

    @InjectView(R.id.text_info)
    TextView textInfo;
    @InjectView(R.id.list_members)
    ListView listMembers;
    @InjectView(R.id.button_begin)
    Button buttonBegin;
    @InjectView(R.id.seekBar)
    SeekBar seekBar;
    @InjectView(R.id.seekBar2)
    SeekBar seekBar2;
    @InjectView(R.id.seekBar3)
    SeekBar seekBar3;
    @InjectView(R.id.seekBar4)
    SeekBar seekBar4;
    private ArrayList<ServerThread> servers = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private Handler handler = new Handler();
    private Sound sound;
    private BassSound bassSound;
    private EGuitarSound eGuitarSound;
    private ChordPlay chordPlay;
    private MediaPlayer mediaPlayer;
    private final String chords[] = {"G", "D", "Em", "Bm", "Am", "C"};
    private final int eChords[][] = {
            new int[]{405, 505, 603}, new int[]{307, 407, 505},
            new int[]{309, 409, 507}, new int[]{304, 404, 502},
            new int[]{302, 402, 500}, new int[]{309, 409, 507},
            new int[]{307, 407, 505}};
    private final int eChords2[][] = {
            new int[]{523, 623}, new int[]{423, 523},
            new int[]{423, 523}, new int[]{423, 523},
            new int[]{423, 523}, new int[]{323, 423, 523},
            new int[]{323, 423, 523}};
    private BleLink guitarBleLink;
    private BleLink drumBleLink;
    private BleLink bassBleLink;
    private BleLink eGuitarBleLink;
    private final int rhythm[] = {0, 0, 1, 1, 0, 1};
    private final boolean eRhythm1[] = {true, false, true, false, false, true, false};
    private final boolean eRhythm2[] = {true, false, true};
    private int rhythmIndex = 0;
    private int chordIndex = 0;
    private int chordIndex2 = 0;
    private int eRhythmIndex = 0;//电吉他
    private int eChordIndex = 0;//电吉他
    private int eChordIndex2 = 0;//电吉他
    private ServerThread guitarDevice;
    private ServerThread drumDevice;
    private ServerThread bassDevice;
    private ServerThread eGuitarDevice;
    private int bassName = 200;
    private final int bassNameList[] = {200, 202, 204, 205, 300, 302, 303, 403};
    private int bassIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        ButterKnife.inject(this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        initView();
        startListen();
        sound = new Sound();
        sound.init(this);
        initSound();
        initBassSound();
        initEGuitarSound();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(Download.getResourcePath() + "/不再犹豫.mp3");
            mediaPlayer.prepare();
        } catch (Exception e) {

        }
        initBleLink();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    private Runnable mediaRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer.isPlaying())
                mediaPlayer.pause();
        }
    };

    private void initBleLink() {
        BleLink.BleListener guitarListener = new BleLink.BleListener() {
            @Override
            public void OnDeviceStateChanged(final BleLink bleLink, BleLink.DeviceState state) {
                if (guitarDevice != null && guitarDevice.getLinkState() != ServerThread.LinkState.Dislink)
                    guitarDevice.write((byte) 0x04, (byte) 0x00, (byte) state.value());
                if (state == BleLink.DeviceState.DEVICE_STATE_LINKED) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x0B, 0x00))) {
                                MyUtil.showToast(RoomActivity2.this, "陀螺仪已打开");
                                handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x0B, 0x01))) {
                                                                MyUtil.showToast(RoomActivity2.this, "选择乐器");
                                                                handler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        if (bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x00, 'G')))//选择乐器鼓
                                                                            MyUtil.showToast(RoomActivity2.this, "选择吉他");
                                                                    }
                                                                }, 500);
                                                            }
                                                        }
                                                    },
                                        500);
                            }
                        }
                    }, 500);
                }
            }

            @Override
            public void OnDataReceived(BleLink bleLink, byte[] data) {
                if (data.length < 10)
                    return;
                if (data[9] == 0)
                    return;
                if (chordIndex != chordIndex2) {
                    rhythmIndex = 0;
                    chordIndex = chordIndex2;
                }
                if (data[6] != rhythm[rhythmIndex])
                    return;
                switch (data[6]) {
                    case 0x00:
                        chordPlay.play(chords[chordIndex], false);
                        break;
                    case 0x01:
                        chordPlay.play(chords[chordIndex], true);
                        break;
                }
                if (guitarDevice != null && guitarDevice.getLinkState() != ServerThread.LinkState.Dislink)
                    guitarDevice.write((byte) 0x05, (byte) 0x00, (byte) rhythmIndex);
                if (rhythmIndex + 1 < rhythm.length)
                    rhythmIndex++;
                else
                    rhythmIndex = 0;
            }
        };
        BleLink.BleListener drumListener = new BleLink.BleListener() {
            @Override
            public void OnDeviceStateChanged(final BleLink bleLink, BleLink.DeviceState state) {
                if (drumDevice != null && drumDevice.getLinkState() != ServerThread.LinkState.Dislink)
                    drumDevice.write((byte) 0x04, (byte) 0x01, (byte) state.value());
                if (state == BleLink.DeviceState.DEVICE_STATE_LINKED) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x0B, 0x00))) {
                                MyUtil.showToast(RoomActivity2.this, "陀螺仪已打开");
                                handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x0B, 0x01))) {
                                                                MyUtil.showToast(RoomActivity2.this, "选择乐器");
                                                                handler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        if (bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x00, 'D')))//选择乐器鼓
                                                                            MyUtil.showToast(RoomActivity2.this, "选择架子鼓");
                                                                    }
                                                                }, 500);
                                                            }
                                                        }
                                                    },
                                        500);
                            }
                        }
                    }, 500);
                }
            }

            @Override
            public void OnDataReceived(BleLink bleLink, byte[] data) {
                if (data.length < 7)
                    return;
                switch (data[6]) {
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
            }
        };
        BleLink.BleListener bassListener = new BleLink.BleListener() {
            @Override
            public void OnDeviceStateChanged(final BleLink bleLink, BleLink.DeviceState state) {
                if (bassDevice != null && bassDevice.getLinkState() != ServerThread.LinkState.Dislink)
                    bassDevice.write((byte) 0x04, (byte) 0x02, (byte) state.value());
                if (state == BleLink.DeviceState.DEVICE_STATE_LINKED) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x0B, 0x00))) {
                                MyUtil.showToast(RoomActivity2.this, "陀螺仪已打开");
                                handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x0B, 0x01))) {
                                                                MyUtil.showToast(RoomActivity2.this, "选择乐器");
                                                                handler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        if (bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x00, 'G')))//选择乐器鼓
                                                                            MyUtil.showToast(RoomActivity2.this, "选择贝斯");
                                                                    }
                                                                }, 500);
                                                            }
                                                        }
                                                    },
                                        500);
                            }
                        }
                    }, 500);
                }
            }

            @Override
            public void OnDataReceived(BleLink bleLink, byte[] data) {
                if (data.length < 10)
                    return;
                if (data[9] == 0)
                    return;
                switch (data[6]) {
                    case 0x00://下扫
                        bassSound.play(bassName);
                        break;
                    case 0x01:

                        break;
                }
            }
        };
        BleLink.BleListener eGuitarListener = new BleLink.BleListener() {
            @Override
            public void OnDeviceStateChanged(final BleLink bleLink, BleLink.DeviceState state) {
                if (eGuitarDevice != null && eGuitarDevice.getLinkState() != ServerThread.LinkState.Dislink)
                    eGuitarDevice.write((byte) 0x04, (byte) 0x03, (byte) state.value());
                if (state == BleLink.DeviceState.DEVICE_STATE_LINKED) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x0B, 0x00))) {
                                MyUtil.showToast(RoomActivity2.this, "陀螺仪已打开");
                                handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x0B, 0x01))) {
                                                                MyUtil.showToast(RoomActivity2.this, "选择乐器");
                                                                handler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        if (bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x00, 'G')))//选择乐器鼓
                                                                            MyUtil.showToast(RoomActivity2.this, "选择电吉他");
                                                                    }
                                                                }, 500);
                                                            }
                                                        }
                                                    },
                                        500);
                            }
                        }
                    }, 500);
                }
            }

            @Override
            public void OnDataReceived(BleLink bleLink, byte[] data) {
                if (data.length < 10)
                    return;
                if (data[9] == 0)
                    return;
                switch (data[6]) {
                    case 0x00:
                        if (eChordIndex != eChordIndex2) {
                            eRhythmIndex = 0;
                            eChordIndex = eChordIndex2;
                        }
                        if (eChordIndex == 7) {
                            if (!mediaPlayer.isPlaying()) {
                                if (mediaPlayer.getCurrentPosition() < mediaPlayer.getDuration())
                                    mediaPlayer.start();
                            }
                            handler.removeCallbacks(mediaRunnable);
                            handler.postDelayed(mediaRunnable, 2000);
                            return;
                        }
                        if (eChordIndex < 5)//0-4
                        {
                            if (eRhythm1[eRhythmIndex])
                                eGuitarSound.play(eChords[eChordIndex]);
                            else
                                eGuitarSound.play(eChords2[eChordIndex]);
                            if (eRhythmIndex + 1 < eRhythm1.length)
                                eRhythmIndex++;
                            else
                                eRhythmIndex = 0;
                        } else {
                            if (eRhythm2[eRhythmIndex])
                                eGuitarSound.play(eChords[eChordIndex]);
                            else
                                eGuitarSound.play(eChords2[eChordIndex]);
                            if (eRhythmIndex + 1 < eRhythm2.length)
                                eRhythmIndex++;
                            else
                                eRhythmIndex = 0;
                        }
                        break;
                    case 0x01:

                        break;
                }
                if (eGuitarDevice != null && eGuitarDevice.getLinkState() != ServerThread.LinkState.Dislink)
                    eGuitarDevice.write((byte) 0x05, (byte) 0x03, (byte) eRhythmIndex);
            }
        };
        guitarBleLink = new BleLink(this);
        drumBleLink = new BleLink(this);
        bassBleLink = new BleLink(this);
        eGuitarBleLink = new BleLink(this);
        guitarBleLink.setBleListener(guitarListener);
        drumBleLink.setBleListener(drumListener);
        bassBleLink.setBleListener(bassListener);
        eGuitarBleLink.setBleListener(eGuitarListener);
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

    private void initBassSound() {
        int nameList[] = new int[]{200, 202, 204, 205, 300, 302, 303, 403};
        bassSound = new BassSound();
        bassSound.init(this, nameList);
    }

    private void initEGuitarSound() {
        int nameList[] = new int[]{405, 505, 603, 307, 407, 309, 409, 507, 304, 404, 502, 302, 402, 500, 323, 423, 523, 623};
        eGuitarSound = new EGuitarSound();
        eGuitarSound.init(this, nameList);
    }

    private void initView() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listMembers.setAdapter(adapter);
        textInfo.setText("How to enter room?\nPlease input:" + IpAdressUtils.getIp(this));
        SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar == RoomActivity2.this.seekBar) {
                    ChordPlay.setVolume(progress);
                }
                if (seekBar == RoomActivity2.this.seekBar2) {
                    Sound.setVolume((byte) progress);
                }
                if (seekBar == RoomActivity2.this.seekBar3) {
                    EGuitarSound.setVolume((byte) progress);
                }
                if (seekBar == RoomActivity2.this.seekBar4) {
                    BassSound.setVolume((byte) progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
        seekBar.setOnSeekBarChangeListener(seekListener);
        seekBar2.setOnSeekBarChangeListener(seekListener);
        seekBar3.setOnSeekBarChangeListener(seekListener);
        seekBar4.setOnSeekBarChangeListener(seekListener);
    }

    @OnClick(R.id.button_begin)
    public void onViewClicked() {
        if (servers.size() > 0) {
            ServerThread thread;
            for (int i = 0; i < servers.size(); i++) {
                thread = servers.get(i);
                thread.write((byte) 0x01, (byte) 0x01);
            }
        } else {
            MyUtil.showToast(this, "等待玩家加入房间");
        }
    }

    private boolean isUiRun = false;
    private Runnable listRunnable = new Runnable() {
        @Override
        public void run() {
            adapter.clear();
//            if (MainBandActivity.instrumentType == 0)
//                adapter.add("房主——吉他");
//            if (MainBandActivity.instrumentType == 1)
//                adapter.add("房主——架子鼓");
//            if (MainBandActivity.instrumentType == 2)
//                adapter.add("房主——电吉他");
//            if (MainBandActivity.instrumentType == 3)
//                adapter.add("房主——贝斯");
            for (int i = 0; i < servers.size(); i++) {
                adapter.add(servers.get(i).getTag());
            }
            adapter.notifyDataSetChanged();
            isUiRun = false;
        }
    };

    private ServerThread.OnServerListener serverListener = new ServerThread.OnServerListener() {
        @Override
        public void OnStateChanged(ServerThread server, ServerThread.LinkState state) {
            synchronized (this) {
                switch (state) {
                    case Linked:
                        break;
                    case Verified:
                        servers.add(server);
                        break;
                    case Dislink:
                        servers.remove(server);
                        if (server == guitarDevice)
                            guitarBleLink.dislink();
                        if (server == drumDevice)
                            drumBleLink.dislink();
                        if (server == bassDevice)
                            bassBleLink.dislink();
                        if (server == eGuitarDevice)
                            eGuitarBleLink.dislink();
                        break;
                }
                isUiRun = true;
                runOnUiThread(listRunnable);
                while (isUiRun) ;
            }
        }

        @Override
        public void OnDataReceived(ServerThread server, final byte[] value, int length) {
            switch (value[0]) {
                case 0x00:
                    if (value[1] == 0x00)//吉他
                    {
                        guitarDevice = server;
                        String name = "吉他手";
                        //macName.put(device.getAddress(), name);
                    } else if (value[1] == 0x01)//鼓
                    {
                        drumDevice = server;
                        String name = "鼓手";
                        //macName.put(device.getAddress(), name);
                    }
                    break;
                case 0x03:
                    if (value[1] == 0x00)//连接吉他
                    {
                        guitarDevice = server;
                        guitarBleLink.scanLink(MyUtil.bytes2Addr(value, 2));
                    } else if (value[1] == 0x01)//连接鼓
                    {
                        drumDevice = server;
                        drumBleLink.scanLink(MyUtil.bytes2Addr(value, 2));
                    } else if (value[1] == 0x02)//连接贝斯
                    {
                        bassDevice = server;
                        bassBleLink.scanLink(MyUtil.bytes2Addr(value, 2));
                    } else if (value[1] == 0x03)//连接电吉他
                    {
                        eGuitarDevice = server;
                        eGuitarBleLink.scanLink(MyUtil.bytes2Addr(value, 2));
                    }
                    break;
                case 0x05:
                    if (value[1] == 0x00)//吉他
                    {
                        rhythmIndex = 0;
                        chordIndex2 = value[2];
                        guitarDevice = server;
                    } else if (value[1] == 0x01)//鼓
                    {
                        drumDevice = server;
                    } else if (value[1] == 0x02)//贝斯
                    {
                        bassIndex = value[2];
                        bassName = bassNameList[bassIndex];
                        bassDevice = server;
                    } else if (value[1] == 0x03)//电吉他
                    {
                        eGuitarSound.silence();
                        eRhythmIndex = 0;
                        eChordIndex2 = value[2];
                        eGuitarDevice = server;
                        if (eChordIndex2 == 7) {
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.pause();
                            }
                            mediaPlayer.seekTo(0);
                        }
                    }
                    break;
            }
        }
    };
    ServerSocket serverSocket;

    public void startListen() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    serverSocket = new ServerSocket(8888);
                    Log.i("RoomActivity", "startListen");
                    Socket socket;
                    while (true) {
                        socket = serverSocket.accept();
                        ServerThread serverThread = new ServerThread(socket);
                        serverThread.setListener(serverListener);
                        serverThread.start();
                    }
                } catch (Exception e) {
                    Log.i("RoomActivity", "endListen");
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onDestroy() {
        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (Exception e) {

        }
        for (int i = 0; i < servers.size(); i++) {
            servers.get(i).close();
        }
        sound.release();
        bassSound.release();
        eGuitarSound.release();
        mediaPlayer.release();
        super.onDestroy();
    }
}
