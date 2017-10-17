package com.example.panpan.amazingdrum.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.panpan.amazingdrum.BleJoin;
import com.example.panpan.amazingdrum.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class JoinActivityBle extends Activity {

    @InjectView(R.id.edit_room)
    EditText editRoom;
    @InjectView(R.id.button_join)
    Button buttonJoin;
    @InjectView(R.id.edit_name)
    EditText editName;
    private BleJoin bleJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.button_join)
    public void onViewClicked() {
        if (buttonJoin.getText().toString().equals("加入"))
            join();
        if (buttonJoin.getText().toString().equals("退出")) {
            close();
        }
    }

    private BleJoin.OnJoinListener onBleJoinListener = new BleJoin.OnJoinListener() {
        @Override
        public void OnStateChanged(BleJoin thread, BleJoin.State state) {
            switch (state) {
                case Linked:
                    break;
                case Verified:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            buttonJoin.setEnabled(true);
                            buttonJoin.setText("退出");
                        }
                    });
                    break;
                case Dislink:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (buttonJoin.getText().toString().contains("正在加入")) {
                                Toast.makeText(JoinActivityBle.this, "加入失败", Toast.LENGTH_LONG).show();
                            }
                            buttonJoin.setEnabled(true);
                            buttonJoin.setText("加入");
                        }
                    });
                    break;
            }
        }

        @Override
        public void OnDataReceived(BleJoin thread, byte[] data, int length) {
            if (data[0] == 0x01) {
                if (MainBandActivity.instrumentType == 0x01) {
                    PlayDrumActivityBle.bleJoin = bleJoin;
                    startActivity(new Intent(JoinActivityBle.this, PlayDrumActivityBle.class));
                    finish();
                }
                if (MainBandActivity.instrumentType == 0x00) {
                    PlayGuitarActivityBle2.bleJoin = bleJoin;
                    startActivity(new Intent(JoinActivityBle.this, PlayGuitarActivityBle2.class));
                    finish();
                }
            }
        }
    };

    private void join() {
        buttonJoin.setEnabled(false);
        buttonJoin.setText("正在加入...");
        String host = editRoom.getText().toString();
        String name = editName.getText().toString();
        if (MainBandActivity.instrumentType == 0)
            name += "——吉他";
        if (MainBandActivity.instrumentType == 1)
            name += "——架子鼓";
        if (MainBandActivity.instrumentType == 2)
            name += "——电吉他";
        if (MainBandActivity.instrumentType == 3)
            name += "——贝斯";
        bleJoin = new BleJoin(this, name);
        bleJoin.setListener(onBleJoinListener);
        bleJoin.startScan();
    }

    private void close() {
        buttonJoin.setEnabled(false);
        buttonJoin.setText("正在退出...");
        if (bleJoin != null)
            bleJoin.close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
