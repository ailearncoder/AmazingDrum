package com.example.panpan.amazingdrum.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.ArrayMap;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.panpan.amazingdrum.BleJoin;
import com.example.panpan.amazingdrum.BleLink;
import com.example.panpan.amazingdrum.R;
import com.example.panpan.amazingdrum.custom.MyUtil;
import com.example.panpan.amazingdrum.sound.ChordPlay;
import com.example.panpan.amazingdrum.util.Download;
import com.example.panpan.amazingdrum.util.UartDataDeal;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PlayGuitarActivityBle extends Activity {
    public static BleJoin bleJoin;
    @InjectView(R.id.play_button_1)
    Button playButton1;
    @InjectView(R.id.play_button_2)
    Button playButton2;
    @InjectView(R.id.play_button_3)
    Button playButton3;
    @InjectView(R.id.play_button_4)
    Button playButton4;
    @InjectView(R.id.play_button_5)
    Button playButton5;
    @InjectView(R.id.play_button_6)
    Button playButton6;
    @InjectView(R.id.device_info_text)
    TextView deviceInfoText;
    @InjectView(R.id.device_setlect_btn)
    Button deviceSetlectBtn;
    private byte[] sendData = new byte[8];
    private ChordPlay chordPlay;
    private BleLink bleLink;
    private Handler handler = new Handler();
    private String chordName = "C";
    private final int rhythm[] = {0, 0, 1, 1, 0, 1};
    private int rhythmIndex = 0;
    private byte chordIndex=0;
    private ArrayMap<String,Byte> chordMap=new ArrayMap<>();

    @SuppressWarnings("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_guitar);
        ButterKnife.inject(this);
        previousButton = playButton1;
        if (bleJoin != null)
            bleJoin.setListener(bleJoinListener);
        playButton1.setOnTouchListener(onPlayTouchListener);
        playButton2.setOnTouchListener(onPlayTouchListener);
        playButton3.setOnTouchListener(onPlayTouchListener);
        playButton4.setOnTouchListener(onPlayTouchListener);
        playButton5.setOnTouchListener(onPlayTouchListener);
        playButton6.setOnTouchListener(onPlayTouchListener);
        playButton1.setId(0);
        playButton2.setId(1);
        playButton3.setId(2);
        playButton4.setId(3);
        playButton5.setId(4);
        playButton6.setId(5);
        bleLink = new BleLink(this);
        bleLink.setBleListener(bleListener);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        initSound();
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
            chordMap.put("G",(byte)0);
            chordMap.put("D",(byte)1);
            chordMap.put("Em",(byte)2);
            chordMap.put("Bm",(byte)3);
            chordMap.put("Am",(byte)4);
            chordMap.put("C",(byte)5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        chordPlay.endLoadSingleChordFile();
    }

    private BleJoin.OnJoinListener bleJoinListener = new BleJoin.OnJoinListener() {
        @Override
        public void OnStateChanged(BleJoin thread, BleJoin.State state) {

        }

        @Override
        public void OnDataReceived(BleJoin thread, byte[] data, int length) {

        }
    };

    @Override
    protected void onDestroy() {
        if (bleJoin != null)
            bleJoin.close();
        bleJoin = null;
        bleLink.dislink();
        chordPlay.release();
        super.onDestroy();
    }

    private Button previousButton;
    private View.OnTouchListener onPlayTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int id = v.getId();
                rhythmIndex = 0;
                chordName = ((Button) v).getText().toString();
                chordIndex=(byte)id;
                ((Button) v).setTextColor(Color.BLUE);
                previousButton.setTextColor(Color.BLACK);
                previousButton = (Button) v;
                //chordPlay.play(((Button)v).getText().toString(),false);
            }
            return false;
        }
    };

    ProgressDialog progressDialog;

    private void showProgressDialog(String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("提示");
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(msg);
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void cancelProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.cancel();
    }

    private BleLink.DeviceState bleState;
    private boolean isUIRun = false;
    private Runnable bleStateRunnable = new Runnable() {
        @Override
        public void run() {
            switch (bleState) {
                case DEVICE_STATE_DISLINK:
                    deviceInfoText.setText("设备未连接");
                    cancelProgressDialog();
                    break;
                case DEVICE_STATE_DISCOVERING:
                    showProgressDialog("发现服务");
                    break;
                case DEVICE_STATE_CHARACTER:
                    showProgressDialog("发现特征");
                    break;
                case DEVICE_STATE_LINKING:
                    deviceInfoText.setText("设备连接中:" + bleLink.getName() + "(" + bleLink.getAddress() + ")");
                    showProgressDialog("设备连接中:" + bleLink.getName() + "(" + bleLink.getAddress() + ")");
                    break;
                case DEVICE_STATE_LINKED:
                    deviceInfoText.setText("设备已连接:" + bleLink.getName() + "(" + bleLink.getAddress() + ")");
                    cancelProgressDialog();
                    break;
                case DEVICE_STATE_LINKFAILED:
                    cancelProgressDialog();
                    MyUtil.showToast(PlayGuitarActivityBle.this, "设备连接失败\n" + bleLink.getErrorMsg());
                    break;
                case DEVICE_STATE_LINKLOST:
                    cancelProgressDialog();
                    MyUtil.showToast(PlayGuitarActivityBle.this, "设备连接丢失\n" + bleLink.getErrorMsg());
                    break;
            }
            isUIRun = false;
        }
    };
    private BleLink.BleListener bleListener = new BleLink.BleListener() {
        @Override
        public void OnDeviceStateChanged(BleLink bleLink, BleLink.DeviceState state) {
            isUIRun = true;
            bleState = state;
            runOnUiThread(bleStateRunnable);
            while (isUIRun) ;
            if (state == BleLink.DeviceState.DEVICE_STATE_LINKED) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (PlayGuitarActivityBle.this.bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x0B, 0x00))) {
                            MyUtil.showToast(PlayGuitarActivityBle.this, "陀螺仪已打开");
                            handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (PlayGuitarActivityBle.this.bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x0B, 0x01))) {
                                                            MyUtil.showToast(PlayGuitarActivityBle.this, "选择吉他");
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
            sendData[0] = (byte) 0x02;
            sendData[1] = (byte) MainBandActivity.instrumentType;
            sendData[2] = data[6];
            sendData[3] =chordIndex;
            if (data[9] == 0)
                return;
            if (data[6] != rhythm[rhythmIndex])
                return;
            rhythmIndex++;
            rhythmIndex %= 6;
            if (bleJoin != null)
                bleJoin.sendData(sendData);
            switch (data[6]) {
                case 0x00:
                    chordPlay.play(chordName, false);
                    break;
                case 0x01:
                    chordPlay.play(chordName, true);
                    break;
            }
        }
    };

    @OnClick(R.id.device_setlect_btn)
    public void onViewClicked() {
        Intent intent = new Intent(this, DeviceSelectActivity.class);
        startActivityForResult(intent, 0x01);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x01) {
            if (resultCode == RESULT_OK) {
                String name = data.getStringExtra("name");
                String addr = data.getStringExtra("addr");
                bleLink.link(addr);
            }
        }
    }
}
