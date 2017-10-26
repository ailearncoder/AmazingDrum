package com.example.panpan.amazingdrum.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.panpan.amazingdrum.BleLink;
import com.example.panpan.amazingdrum.JoinThread;
import com.example.panpan.amazingdrum.R;
import com.example.panpan.amazingdrum.custom.MyUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class PlayDrumActivity2 extends Activity {
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
    private String name, address;

    @SuppressWarnings("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_drum);
        ButterKnife.inject(this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
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
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    private JoinThread.OnJoinListener joinListener = new JoinThread.OnJoinListener() {
        @Override
        public void OnStateChanged(JoinThread thread, JoinThread.State state) {
            switch (state) {
                case Dislink:
                    finish();
                    break;
            }
        }

        @Override
        public void OnDataReceived(JoinThread thread, byte[] data, int length) {
            switch (data[0]) {
                case 0x04:
                    int index = data[2];
                    isUIRun = true;
                    bleState = BleLink.DeviceState.values()[index];
                    runOnUiThread(bleStateRunnable);
                    while (isUIRun) ;
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        if (joinThread != null)
            joinThread.close();
        joinThread = null;
        super.onDestroy();
    }

    private View.OnTouchListener onPlayTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int id = v.getId();
                byte chordIndex = 0;
                if (id == 0)
                    chordIndex = 4;
                if (id == 1)
                    chordIndex = 8;
                if (id == 2)
                    chordIndex = 1;
                if (id == 3)
                    chordIndex = 2;
                if (joinThread != null)
                    joinThread.write((byte) 0x05, (byte) 0x00, chordIndex);
            }
            return false;
        }
    };

    ProgressDialog progressDialog;

    private void showProgressDialog(String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("提示");
            //progressDialog.setCancelable(false);
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
                case DEVICE_STATE_SCANING:
                    showProgressDialog("正在扫描");
                    break;
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
                    deviceInfoText.setText("设备连接中:" + name + "(" + address + ")");
                    showProgressDialog("设备连接中:" + name + "(" + address + ")");
                    break;
                case DEVICE_STATE_LINKED:
                    deviceInfoText.setText("设备已连接:" + name + "(" + address + ")");
                    cancelProgressDialog();
                    break;
                case DEVICE_STATE_LINKFAILED:
                    cancelProgressDialog();
                    MyUtil.showToast(PlayDrumActivity2.this, "设备连接失败");
                    break;
                case DEVICE_STATE_SCANFAILED:
                    cancelProgressDialog();
                    MyUtil.showToast(PlayDrumActivity2.this, "扫描失败");
                    break;
                case DEVICE_STATE_LINKLOST:
                    cancelProgressDialog();
                    MyUtil.showToast(PlayDrumActivity2.this, "设备连接丢失");
                    break;
            }
            isUIRun = false;
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
                name = data.getStringExtra("name");
                address = data.getStringExtra("addr");
                byte addr[] = MyUtil.addr2Bytes(address);
                byte data2[] = new byte[addr.length + 2];
                data2[0] = 0x03;
                data2[1] = 0x01;
                System.arraycopy(addr, 0, data2, 2, addr.length);
                joinThread.write(data2);
            }
        }
    }
}
