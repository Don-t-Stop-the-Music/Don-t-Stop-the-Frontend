package com.dontstopthemusic.dontstopthemusic;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.util.Log;


import java.io.IOException;
import java.util.UUID;

class ConnectThread extends Thread implements Runnable{

    static UUID MY_UUID = new UUID(0x1e0ca4ea299d4335L, 0x93eb27fcfe7fa848L);//using the 'base' bluetooth UUID
    static String TAG = "what is a tag?";
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    public ConnectThread(BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket temp_socket = null;
        mmDevice = device;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            temp_socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        } catch (SecurityException e) {
            Log.e(TAG, "Socket's create() method failed (lacking security privilege)", e);
        }
        mmSocket = temp_socket;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        // bluetoothAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        } catch (SecurityException securityException) {
            // Unable to access; close the socket and return.

            Log.e(TAG, "Missing permission to access socket", securityException);

            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;

        }

        System.out.println("Successfully Accessed Socket!");

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        //manageMyConnectedSocket(mmSocket);
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}