package com.example.panpan.amazingdrum.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.panpan.amazingdrum.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DeviceSelectActivity extends Activity {

    @InjectView(R.id.listView)
    ListView listView;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayMap<Integer, String> deviceMap = new ArrayMap<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_select);
        ButterKnife.inject(this);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent();
                intent.putExtra("name","");
                intent.putExtra("addr",deviceMap.get(position));
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        initBle();
        beginScan();
    }

    private void initBle() {
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            finish();
            return;
        }
        mBluetoothAdapter.enable();
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String name = device.getName();
            String addr = device.getAddress();
            int rssi = result.getRssi();
            if ("bong4".equals(name))//AeroBand
            {
                if (deviceMap.containsValue(addr)) {
                } else {
                    deviceMap.put(adapter.getCount(), addr);
                    adapter.add("Name:" + name + "\nMac:" + addr);
                    adapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
        }
    };

    private void beginScan() {
        mBluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
    }

    @Override
    protected void onDestroy() {
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
        super.onDestroy();
    }
}
