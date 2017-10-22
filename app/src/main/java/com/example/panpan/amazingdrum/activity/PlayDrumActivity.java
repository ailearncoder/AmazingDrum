package com.example.panpan.amazingdrum.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.panpan.amazingdrum.BleLink;
import com.example.panpan.amazingdrum.JoinThread;
import com.example.panpan.amazingdrum.R;
import com.example.panpan.amazingdrum.Sound;
import com.example.panpan.amazingdrum.custom.MyUtil;
import com.example.panpan.amazingdrum.util.UartDataDeal;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PlayDrumActivity extends Activity {
    public JoinThread joinThread;
    @InjectView(R.id.play_button_6)
    Button playButton1;
    @InjectView(R.id.play_button_2)
    Button playButton2;
    @InjectView(R.id.play_button_3)
    Button playButton3;
    @InjectView(R.id.play_button_4)
    Button playButton4;
    @InjectView(R.id.device_info_text)
    TextView deviceInfoText;
    @InjectView(R.id.device_setlect_btn)
    Button deviceSetlectBtn;
    private byte[] sendData = new byte[8];
    private Sound sound;
    private BleLink bleLink;
    private Handler handler = new Handler();

    @SuppressWarnings("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_drum);
        ButterKnife.inject(this);
        joinThread = PlayActivity.joinThread;
        if (joinThread != null)
            joinThread.setListener(joinListener);
        playButton1.setOnTouchListener(onPlayTouchListener);
        playButton2.setOnTouchListener(onPlayTouchListener);
        playButton3.setOnTouchListener(onPlayTouchListener);
        playButton4.setOnTouchListener(onPlayTouchListener);
        playButton1.setId(0);
        playButton2.setId(1);
        playButton3.setId(2);
        playButton4.setId(3);
        sound = new Sound();
        sound.init(this);
        bleLink = new BleLink(this);
        bleLink.setBleListener(bleListener);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    private JoinThread.OnJoinListener joinListener = new JoinThread.OnJoinListener() {
        @Override
        public void OnStateChanged(JoinThread thread, JoinThread.State state) {

        }

        @Override
        public void OnDataReceived(JoinThread thread, byte[] data, int length) {

        }
    };

    @Override
    protected void onDestroy() {
        if (joinThread != null)
            joinThread.close();
        joinThread = null;
        bleLink.dislink();
        sound.release();
        super.onDestroy();
    }

    private View.OnTouchListener onPlayTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int id = v.getId();
                sendData[0] = (byte) 0x02;
                sendData[1] = (byte) MainBandActivity.instrumentType;
                if (id == 0)
                    sendData[2] = 4;
                if (id == 1)
                    sendData[2] = 8;
                if (id == 2)
                    sendData[2] = 1;
                if (id == 3)
                    sendData[2] = 2;
                if (joinThread != null)
                    joinThread.write(sendData);
                sound.play(id % 4);
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
                    MyUtil.showToast(PlayDrumActivity.this, "设备连接失败\n" + bleLink.getErrorMsg());
                    break;
                case DEVICE_STATE_LINKLOST:
                    cancelProgressDialog();
                    MyUtil.showToast(PlayDrumActivity.this, "设备连接丢失\n" + bleLink.getErrorMsg());
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
                        if (PlayDrumActivity.this.bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x0B, 0x00))) {
                            MyUtil.showToast(PlayDrumActivity.this, "陀螺仪已打开");
                            handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (PlayDrumActivity.this.bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x0B, 0x01))) {
                                                            MyUtil.showToast(PlayDrumActivity.this, "选择乐器");
                                                            handler.postDelayed(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    if (PlayDrumActivity.this.bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x00, 'D')))//选择乐器鼓
                                                                        MyUtil.showToast(PlayDrumActivity.this, "选择架子鼓");
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
            sendData[0] = (byte) 0x02;
            sendData[1] = (byte) MainBandActivity.instrumentType;
            sendData[2] = data[6];
            if (joinThread != null)
                joinThread.write(sendData);
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
