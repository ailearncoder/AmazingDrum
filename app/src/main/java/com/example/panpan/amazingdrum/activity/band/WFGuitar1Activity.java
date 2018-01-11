package com.example.panpan.amazingdrum.activity.band;

import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.example.panpan.amazingdrum.BleLink;
import com.example.panpan.amazingdrum.R;
import com.example.panpan.amazingdrum.custom.MyUtil;
import com.example.panpan.amazingdrum.util.FlashLight;
import com.example.panpan.amazingdrum.util.UartDataDeal;

import java.util.HashMap;

/**
 * 王妃吉他1
 */
public class WFGuitar1Activity extends Activity implements OnTouchListener {
    private final int id[] = new int[]{R.id.button1, R.id.button2,
            R.id.button3, R.id.button4, R.id.button5, R.id.button6,
            R.id.button7, R.id.button8, R.id.button9};
    private final Button button[] = new Button[id.length];

    private final String order1[] = new String[]{"#F", "#F", "500", "502",
            "504", "402", "504", "502", "500", "602", "600"};
    private final String order2[] = new String[]{"#F", "#F", "500", "502",
            "504", "402", "404", "502", "500", "602", "600"};
    private final String order3[] = new String[]{"#F", "#F", "500", "502",
            "504", "500", "502"};
    private final String files[] = new String[]{"F", "E", "D", "A", "602",
            "600", "504", "502", "500", "404", "402", "#G", "#F"};
    private final String path = "/sdcard/王妃/guitar1/";
    private final HashMap<String, Integer> soundIdHashMap = new HashMap<String, Integer>();
    private int index = 0;
    private SoundPool soundPool;
    private boolean isSlience;
    private BleLink bleLink = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wf_guitar1);
        bleLink=WFActivity.bleLink;
        for (int i = 0; i < button.length; i++) {
            button[i] = (Button) findViewById(id[i]);
            button[i].setOnTouchListener(this);
        }
        if (FlashLight.init(this))
            FlashLight.startAutoFlash();
        soundPool = new SoundPool(files.length, AudioManager.STREAM_MUSIC, 0);
        int id;
        for (int i = 0; i < files.length; i++) {
            id = soundPool.load(path + files[i] + ".ogg", 1);
            soundIdHashMap.put(files[i], id);
        }
        initBluetooth();
    }

    public void setVolume(View v) {
        if (isSlience) {
            isSlience = false;
            v.setBackgroundResource(R.drawable.volume_open);
        } else {
            isSlience = true;
            v.setBackgroundResource(R.drawable.volume_close);
        }
    }

    private void initBluetooth() {
        bleLink.setBleListener(new BleLink.BleListener() {
            @Override
            public void OnDeviceStateChanged(BleLink bleLink, BleLink.DeviceState state) {

            }

            @Override
            public void OnDataReceived(BleLink bleLink, byte[] data) {
                int orderCode = data[5];
                if (orderCode == 0) {
                    if (!isSlience)
                        play(previousButton);
                }
            }
        });
        // 设置乐器为吉他
        if (bleLink.sendData(UartDataDeal.sendCommand(0x01, 0x00, 'G')))
            MyUtil.showToast(this, "已设置乐器电吉他");
        else {
            bleLink.dislink();
            MyUtil.showToast(this, "蓝牙连接断开");
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        FlashLight.isAutoFlash = false;
        soundPool.release();
        //bleLink.dislink();
    }

    private int previousButton = 0;
    private int streamID;

    private void play(int button) {
        FlashLight.isBegin = true;
        if (button != previousButton) {
            previousButton = button;
            index = 0;
        }
        soundPool.stop(streamID);
        switch (button) {
            case 0:
                index %= order1.length;
                streamID = soundPool.play(soundIdHashMap.get(order1[index]), 1, 1,
                        1, 0, 1);
                index++;
                break;
            case 1:
                index %= order2.length;
                streamID = soundPool.play(soundIdHashMap.get(order2[index]), 1, 1,
                        1, 0, 1);
                index++;
                break;
            case 2:
                index %= order3.length;
                streamID = soundPool.play(soundIdHashMap.get(order3[index]), 1, 1,
                        1, 0, 1);
                index++;
                break;
            case 3:
                streamID = soundPool.play(soundIdHashMap.get("#F"), 1, 1, 1, 0, 1);
                break;
            case 4:
                streamID = soundPool.play(soundIdHashMap.get("D"), 1, 1, 1, 0, 1);
                break;
            case 5:
                streamID = soundPool.play(soundIdHashMap.get("E"), 1, 1, 1, 0, 1);
                break;
            case 6:
                streamID = soundPool.play(soundIdHashMap.get("A"), 1, 1, 1, 0, 1);
                break;
            case 7:
                streamID = soundPool.play(soundIdHashMap.get("#G"), 1, 1, 1, 0, 1);
                break;
            case 8:
                streamID = soundPool.play(soundIdHashMap.get("F"), 1, 1, 1, 0, 1);
                break;
            default:
                break;
        }
        view = this.button[button];
        handler.removeCallbacks(myRunnable);
        handler.removeCallbacks(myRunnable2);
        handler.post(myRunnable2);
        handler.postDelayed(myRunnable, 100);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int id = v.getId();
            for (int i = 0; i < button.length; i++) {
                if (this.id[i] == id) {
                    // play(i);
                    button[previousButton]
                            .setBackgroundResource(R.drawable.band_button);
                    button[i].setBackgroundResource(R.drawable.band_button2);
                    index = 0;
                    previousButton = i;
                    break;
                }
            }
            v.performClick();
            // v.setScaleX(0.95f);
            // v.setScaleY(0.95f);
            // v.invalidate();
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // v.setScaleX(1f);
            // v.setScaleY(1f);
            // v.invalidate();
        }
        return false;
    }

    private final Handler handler = new Handler();
    private View view;
    private final Runnable myRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            view.setScaleX(1f);
            view.setScaleY(1f);
            view.invalidate();
        }
    };
    private final Runnable myRunnable2 = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            view.setScaleX(0.95f);
            view.setScaleY(0.95f);
            view.invalidate();
        }
    };
}
