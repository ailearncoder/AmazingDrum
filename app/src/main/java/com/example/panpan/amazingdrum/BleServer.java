package com.example.panpan.amazingdrum;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.util.Log;

import com.example.panpan.amazingdrum.custom.MyUtil;

import java.util.List;
import java.util.UUID;

import static android.content.Context.BLUETOOTH_SERVICE;

/**
 * Created by PanXuesen on 2017/10/14.
 */
public class BleServer {
    public static final String SERVER_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String RX_UUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String TX_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String DESC_UUID = "00002902-0000-1000-8000-00805f9b34fb";
    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private BluetoothGattServer bluetoothGattServer;
    private final String TAG = "BleServer";
    private OnServerListener listener;

    public BleServer(Context context) {
        this.context = context;
        bleInit();
        bleAdvInit();
        bleServerInit();
    }

    private boolean bleInit() {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            MyUtil.showToast(context, "该设备不支持蓝牙低功耗通讯");
            return false;
        }

        bluetoothManager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);

        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            MyUtil.showToast(context, "该设备不支持蓝牙低功耗通讯");
            return false;
        }

        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        if (bluetoothLeAdvertiser == null) {
            MyUtil.showToast(context, "该设备不支持蓝牙低功耗从设备通讯");
            return false;
        }
        return true;
    }

    private void bleAdvInit() {
        AdvertiseSettings.Builder settingBuilder = new AdvertiseSettings.Builder();
        settingBuilder.setConnectable(true);
        settingBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        settingBuilder.setTimeout(0); //我填过别的，但是不能广播。后来我就坚定的0了
        settingBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        AdvertiseSettings settings = settingBuilder.build();
        //广播参数
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        if (!bluetoothAdapter.setName("AeroBandRoom"))
            MyUtil.showToast(context, "蓝牙名称设置失败");
        dataBuilder.setIncludeDeviceName(true);
        dataBuilder.setIncludeTxPowerLevel(true);
        dataBuilder.addServiceUuid(ParcelUuid.fromString("00001234-0000-1000-8000-00805f9b34fb")); //可自定义UUID，看看官方有没有定义哦
        AdvertiseData data = dataBuilder.build();
        bluetoothLeAdvertiser.startAdvertising(settings, data, advertiseCallback);
    }

    BluetoothGattCharacteristic characteristicRead;
    BluetoothGattCharacteristic characteristicWrite;

    private void bleServerInit() {
        bluetoothGattServer = bluetoothManager.openGattServer(context,
                bluetoothGattServerCallback);
        BluetoothGattService service = new BluetoothGattService(UUID.fromString(SERVER_UUID),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        //特征值读写设置
        characteristicRead = new BluetoothGattCharacteristic(UUID.fromString(TX_UUID),
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
        BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(UUID.fromString(DESC_UUID), BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        //characteristicRead.setValue(new byte[]{0x00, 0x01, 0x02});
        characteristicRead.addDescriptor(descriptor);
        //特征值读写设置
        characteristicWrite = new BluetoothGattCharacteristic(UUID.fromString(RX_UUID),
                BluetoothGattCharacteristic.PROPERTY_WRITE |
                        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);
        service.addCharacteristic(characteristicRead);
        service.addCharacteristic(characteristicWrite);
        bluetoothGattServer.addService(service);
    }

    public boolean sendData(byte... data) {
        characteristicRead.setValue(data);
        BluetoothDevice device;
        List<BluetoothDevice> devices=bluetoothManager.getConnectedDevices(BluetoothProfile.GATT_SERVER);
        for (int i = 0; i < devices.size(); i++) {
            device=devices.get(i);
            if(!bluetoothGattServer.notifyCharacteristicChanged(device, characteristicRead, false))
                return false;
        }
        return true;
    }

    public void close() {
        bluetoothGattServer.clearServices();
        bluetoothGattServer.close();
        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
    }

    private BluetoothGattServerCallback bluetoothGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            OnStateChanged(device, status, newState);
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
            OnDataReceived(device, offset, value);
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, descriptor.getValue());
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            descriptor.setValue(value);
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            Log.i(TAG, "onExecuteWrite:name=" + device.getName() + " address=" + device.getAddress());
            Log.i(TAG, "onExecuteWrite:requestId=" + requestId + " execute=" + execute);
            super.onExecuteWrite(device, requestId, execute);
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            Log.i(TAG, "onMtuChanged:name=" + device.getName() + " address=" + device.getAddress());
            Log.i(TAG, "onMtuChanged:mtu=" + mtu);
            super.onMtuChanged(device, mtu);
        }
    };
    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
        }
    };

    void OnStateChanged(BluetoothDevice device, int status, int newState) {
        if (listener != null)
            listener.onConnectionStateChange(device, status, newState);
    }

    void OnDataReceived(BluetoothDevice device, int offset, byte[] value) {
        if (listener != null)
            listener.OnDataReceived(device, offset, value);
    }

    public OnServerListener getListener() {
        return listener;
    }

    public void setListener(OnServerListener listener) {
        this.listener = listener;
    }

    public interface OnServerListener {
        void onConnectionStateChange(BluetoothDevice device, int status, int newState);

        void OnDataReceived(BluetoothDevice device, int offset, byte[] value);
    }
}
