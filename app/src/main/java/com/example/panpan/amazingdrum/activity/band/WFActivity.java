package com.example.panpan.amazingdrum.activity.band;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.panpan.amazingdrum.BleLink;
import com.example.panpan.amazingdrum.R;
import com.example.panpan.amazingdrum.activity.DeviceSelectActivity;
import com.example.panpan.amazingdrum.activity.PermissionReqiureActivity;
import com.example.panpan.amazingdrum.custom.MyUtil;
import com.example.panpan.amazingdrum.util.UartDataDeal;

/**
 * 王妃
 */
public class WFActivity extends PermissionReqiureActivity implements OnClickListener {
    public static BleLink bleLink = null;
    TextView deviceInfoText;
    Button deviceSetlectBtn;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wf_main);
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
        deviceInfoText = (TextView) findViewById(R.id.device_info_text);
        deviceSetlectBtn = (Button) findViewById(R.id.device_setlect_btn);
        bleLink = new BleLink(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bleLink.setBleListener(new BleLink.BleListener() {
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
                            if (WFActivity.this.bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x0B, 0x00))) {
                                MyUtil.showToast(WFActivity.this, "陀螺仪已打开");
                                handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (WFActivity.this.bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x0B, 0x01))) {
                                                                MyUtil.showToast(WFActivity.this, "选择乐器");
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

            }
        });
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (bleLink.getDeviceState() != BleLink.DeviceState.DEVICE_STATE_LINKED) {
            MyUtil.showToast(this, "蓝牙未就绪");
            return;
        }
        switch (v.getId()) {
            case R.id.button1:
                startActivity(new Intent(this, WFGuitar1Activity.class));
                break;
            case R.id.button2:
                startActivity(new Intent(this, WFGuitar2Activity.class));
                break;
            case R.id.button3:
                startActivity(new Intent(this, WFBassActivity.class));
                break;
            case R.id.button4:
                startActivity(new Intent(this, WFDrumActivity.class));
                break;
            default:
                break;
        }
    }

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
                    MyUtil.showToast(WFActivity.this, "设备连接失败\n" + bleLink.getErrorMsg());
                    break;
                case DEVICE_STATE_LINKLOST:
                    cancelProgressDialog();
                    MyUtil.showToast(WFActivity.this, "设备连接丢失\n" + bleLink.getErrorMsg());
                    break;
            }
            isUIRun = false;
        }
    };

    public void onDeviceSelectClicked(View v) {
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

    @Override
    protected void onDestroy() {
        bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x0F));
        bleLink.dislink();
        super.onDestroy();
    }
}
