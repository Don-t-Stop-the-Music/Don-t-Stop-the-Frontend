package com.dontstopthemusic.dontstopthemusic;

import androidx.lifecycle.ViewModel;

public class DeviceViewModel extends ViewModel {
    private Device device;

    public void assignDevice(BluetoothService bluetoothService) {
        //device = bluetoothService.getFocusDevice();
        device = new FeedbackTestStubDevice();
    }

    public boolean isNull() {
        return (device == null);
    }

    public boolean isConnected() {
        return (!isNull() && device.isConnected());
    }

    public void registerNewDataCallback(Device.NewDataCallback c) {
        device.registerNewDataCallback(c);
    }

    public void registerStatusChangeCallback(Device.StatusChangeCallback c) {
        device.registerStatusChangeCallback(c);
    }

    public void unregisterNewDataCallback(Device.NewDataCallback c) {
        device.unregisterNewDataCallback(c);
    }

    public void unregisterStatusChangeCallback(Device.StatusChangeCallback c) {
        device.unregisterStatusChangeCallback(c);
    }
}
