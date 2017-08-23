package com.example.panpan.amazingdrum;

import android.util.ArrayMap;
import android.widget.SimpleAdapter;

/**
 * Created by PanXuesen on 2017/8/23.
 */

public class MyRunnable implements Runnable {
    public ArrayMap<String, String> map;
    public BleLink.DeviceState state;
    public SimpleAdapter adapter;

    @Override
    public void run() {
        switch (state) {
            case DEVICE_STATE_DISLINK:
                map.put("link","未连接");
                break;
            case DEVICE_STATE_LINKING:
                map.put("link", "连接中...");
                break;
            case DEVICE_STATE_LINKED:
                map.put("link", "已连接");
                break;
            case DEVICE_STATE_LINKFAILED:
                map.put("link", "连接失败");
                break;
            case DEVICE_STATE_LINKLOST:
                map.put("link", "连接丢失");
                break;
            case DEVICE_STATE_DISCOVERING:
                map.put("link", "发现服务");
                break;
            case DEVICE_STATE_CHARACTER:
                map.put("link", "发现特征");
                break;
        }
        adapter.notifyDataSetChanged();
    }
}
