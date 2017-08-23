
package com.example.panpan.amazingdrum;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.util.UUID;

/**
 * Created by panpan on 2017/8/21.
 */

public class BleLink {
    public static final String SERVER_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String RX_UUID     = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String TX_UUID     = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String DESC_UUID   = "00002902-0000-1000-8000-00805f9b34fb";
    public int id=0;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService bleServer;
    private BluetoothGattCharacteristic txChar, rxChar;
    private String address="";
    private BluetoothGattCallback bleCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTING) {

                }
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    if (gatt.discoverServices()) {
                        OnDeviceStateChanged(DeviceState.DEVICE_STATE_DISCOVERING);
                    } else {
                        errorMsg = "发现服务失败！";
                        OnDeviceStateChanged(DeviceState.DEVICE_STATE_LINKFAILED);
                        OnDeviceStateChanged(DeviceState.DEVICE_STATE_DISLINK);
                    }
                }
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    if (deviceState == DeviceState.DEVICE_STATE_LINKING) {
                        OnDeviceStateChanged(DeviceState.DEVICE_STATE_LINKFAILED);
                        OnDeviceStateChanged(DeviceState.DEVICE_STATE_DISLINK);
                    }
                    if (deviceState == DeviceState.DEVICE_STATE_DISCOVERING) {
                        OnDeviceStateChanged(DeviceState.DEVICE_STATE_LINKFAILED);
                        OnDeviceStateChanged(DeviceState.DEVICE_STATE_DISLINK);
                    }
                    if (deviceState == DeviceState.DEVICE_STATE_LINKED) {
                        OnDeviceStateChanged(DeviceState.DEVICE_STATE_LINKLOST);
                        OnDeviceStateChanged(DeviceState.DEVICE_STATE_DISLINK);
                    }
                }
            } else {
                OnDeviceStateChanged(DeviceState.DEVICE_STATE_LINKFAILED);
                OnDeviceStateChanged(DeviceState.DEVICE_STATE_DISLINK);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                bleServer = gatt.getService(UUID.fromString(SERVER_UUID));
                if (bleServer != null) {
                    OnDeviceStateChanged(DeviceState.DEVICE_STATE_CHARACTER);
                    txChar = bleServer.getCharacteristic(UUID.fromString(TX_UUID));
                    rxChar = bleServer.getCharacteristic(UUID.fromString(RX_UUID));
                    if (txChar != null) {
                        if (mBluetoothGatt.setCharacteristicNotification(txChar, true)) {

                        } else {
                            errorMsg = "通知使能失败：mBluetoothGatt.setCharacteristicNotification(txChar, true)";
                            OnDeviceStateChanged(DeviceState.DEVICE_STATE_LINKFAILED);
                            OnDeviceStateChanged(DeviceState.DEVICE_STATE_DISLINK);
                            return;
                        }
                        BluetoothGattDescriptor descriptor = txChar.getDescriptor(
                                UUID.fromString(DESC_UUID));
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        if (mBluetoothGatt.writeDescriptor(descriptor)) {

                        } else {
                            errorMsg = "通知使能失败：mBluetoothGatt.writeDescriptor(descriptor)";
                            OnDeviceStateChanged(DeviceState.DEVICE_STATE_LINKFAILED);
                            OnDeviceStateChanged(DeviceState.DEVICE_STATE_DISLINK);
                        }

                    } else {
                        errorMsg = "未发现TX特性：" + TX_UUID;
                        OnDeviceStateChanged(DeviceState.DEVICE_STATE_LINKFAILED);
                        OnDeviceStateChanged(DeviceState.DEVICE_STATE_DISLINK);
                        return;
                    }
                    if (rxChar != null) {
                        rxChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                        OnDeviceStateChanged(DeviceState.DEVICE_STATE_LINKED);
                    } else {
                        errorMsg = "未发现RX特性：" + RX_UUID;
                        OnDeviceStateChanged(DeviceState.DEVICE_STATE_LINKFAILED);
                        OnDeviceStateChanged(DeviceState.DEVICE_STATE_DISLINK);
                        return;
                    }
                } else {
                    errorMsg = "未发现服务";
                    OnDeviceStateChanged(DeviceState.DEVICE_STATE_LINKFAILED);
                    OnDeviceStateChanged(DeviceState.DEVICE_STATE_DISLINK);
                }
            } else {
                errorMsg = "发现服务操作失败";
                OnDeviceStateChanged(DeviceState.DEVICE_STATE_LINKFAILED);
                OnDeviceStateChanged(DeviceState.DEVICE_STATE_DISLINK);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic == txChar) {
                byte data[] = characteristic.getValue();
                if (data != null)
                    OnDataReceived(data);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setBleListener(BleListener bleListener) {
        this.bleListener = bleListener;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public enum DeviceState {
        DEVICE_STATE_DISLINK,
        DEVICE_STATE_LINKING,
        DEVICE_STATE_LINKED,
        DEVICE_STATE_LINKLOST,
        DEVICE_STATE_LINKFAILED,
        DEVICE_STATE_DISCOVERING,
        DEVICE_STATE_CHARACTER
    }

    private String errorMsg = "";
    private BleListener bleListener;
    private Context context;
    private DeviceState deviceState = DeviceState.DEVICE_STATE_DISLINK;

    public BleLink(Context context) {
        this.context = context;
    }

    public boolean link(BluetoothDevice device) {
        mBluetoothGatt = device.connectGatt(context, false, bleCallBack);
        if (mBluetoothGatt != null) {
            OnDeviceStateChanged(DeviceState.DEVICE_STATE_LINKING);
            return true;
        } else {
            OnDeviceStateChanged(DeviceState.DEVICE_STATE_LINKFAILED);
            OnDeviceStateChanged(DeviceState.DEVICE_STATE_DISLINK);
        }
        return false;
    }

    public void dislink() {
        if (deviceState != DeviceState.DEVICE_STATE_DISLINK) {
            if (mBluetoothGatt != null) {
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            }
            OnDeviceStateChanged(DeviceState.DEVICE_STATE_DISLINK);
        }
    }

    public boolean sendData(byte... data) {
        if(deviceState==DeviceState.DEVICE_STATE_LINKED) {
            rxChar.setValue(data);
            return mBluetoothGatt.writeCharacteristic(rxChar);
        }
        return false;
    }

    void OnDeviceStateChanged(DeviceState state) {
        if (this.deviceState != state) {
            deviceState = state;
            if (state == DeviceState.DEVICE_STATE_DISLINK && mBluetoothGatt != null) {
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            }
            if (bleListener != null)
                bleListener.OnDeviceStateChanged(this, state);
        }
    }

    void OnDataReceived(byte data[]) {
        if (bleListener != null)
            bleListener.OnDataReceived(this, data);
    }

    public interface BleListener {
        void OnDeviceStateChanged(BleLink bleLink, DeviceState state);

        void OnDataReceived(BleLink bleLink, byte data[]);
    }
    public DeviceState getDeviceState()
    {
        return deviceState;
    }
}
