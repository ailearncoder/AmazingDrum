package com.example.panpan.amazingdrum;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;

import java.io.UnsupportedEncodingException;

/**
 * Created by panpan on 2017/10/11.
 */

public class BleJoin extends Thread {
    private OnJoinListener listener;
    private State state = State.None;
    private Context context;
    private BleLink bleLink;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean isScan = false;
    private Handler handler=new Handler();

    public BleJoin(Context context, String name) {
        setName(name);
        this.context = context;
        bleLink = new BleLink(context);
        bleLink.setBleListener(bleListener);
        initBle();
    }

    private void initBle() {
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            return;
        }
        mBluetoothAdapter.enable();
    }

    public void startScan() {
        if (isScan)
            return;
        isScan = true;
        mBluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);
        OnStateChanged(State.BeginScan);
    }

    public void stopScan() {
        if (!isScan)
            return;
        isScan = false;
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
        OnStateChanged(State.EndScan);
    }

    public boolean sendData(byte... data) {
        return bleLink.sendData(data);
    }

    public void close() {
        bleLink.dislink();
    }

    void OnStateChanged(State state) {
        if (this.state != state) {
            this.state = state;
            if (listener != null)
                listener.OnStateChanged(this, state);
        }
    }

    void OnDataReceived(byte[] data, int length) {
        if (listener != null)
            listener.OnDataReceived(this, data, length);
    }

    public OnJoinListener getListener() {
        return listener;
    }

    public void setListener(OnJoinListener listener) {
        this.listener = listener;
    }

    public interface OnJoinListener {
        void OnStateChanged(BleJoin thread, State state);

        void OnDataReceived(BleJoin thread, byte[] data, int length);
    }

    public enum State {
        None,
        BeginScan,
        EndScan,
        Linked,
        Verified,
        Dislink
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if ("AeroBandRoom".equals(device.getName())) {
                stopScan();
                bleLink.link(device);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            isScan = false;
            OnStateChanged(State.EndScan);
        }
    };

    private void sendName() {
        try {
            byte name[] = getName().getBytes("UTF-8");
            byte name2[] = new byte[name.length + 1];
            System.arraycopy(name, 0, name2, 1, name.length);
            name2[0] = 0;
            if (sendData(name2))
                OnStateChanged(State.Verified);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private BleLink.BleListener bleListener = new BleLink.BleListener() {
        @Override
        public void OnDeviceStateChanged(BleLink bleLink, BleLink.DeviceState state) {
            switch (state) {
                case DEVICE_STATE_DISLINK:
                    OnStateChanged(State.Dislink);
                    break;
                case DEVICE_STATE_DISCOVERING:

                    break;
                case DEVICE_STATE_CHARACTER:

                    break;
                case DEVICE_STATE_LINKING:

                    break;
                case DEVICE_STATE_LINKED:
                    OnStateChanged(State.Linked);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendName();
                        }
                    },500);
                    break;
                case DEVICE_STATE_LINKFAILED:

                    break;
                case DEVICE_STATE_LINKLOST:

                    break;
            }
        }

        @Override
        public void OnDataReceived(BleLink bleLink, byte[] data) {
            BleJoin.this.OnDataReceived(data, data.length);
        }
    };
}
