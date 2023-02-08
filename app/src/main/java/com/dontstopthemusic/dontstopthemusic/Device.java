package com.dontstopthemusic.dontstopthemusic;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;

public class Device
{

	/* The bluetooth device itself */
	private final BluetoothDevice mBluetoothDevice;

	/* The socket for the device */
	private BluetoothSocket mBluetoothSocket;

	/* Whether the device is currently trying to connect */
	private boolean connecting = false;

	/* A set of callbacks for status changes */
	private final HashSet<DeviceStatusChangeCallback> statusChangeCallbacks = new HashSet<> ();



	/**
	 * @param bluetoothDevice The Bluetooth device.
	 */
	public Device ( BluetoothDevice bluetoothDevice )
	{
		/* Set the device */
		mBluetoothDevice = bluetoothDevice;
	}



	/**
	 * @return The internal BluetoothDevice instance.
	 */
	public BluetoothDevice getInfo ()
	{
		return mBluetoothDevice;
	}

	/**
	 * @return The internal BluetoothSocket instance.
	 */
	public BluetoothSocket getSocket ()
	{
		return mBluetoothSocket;
	}

	/**
	 * @return Whether the device is currently connected.
	 */
	public boolean isConnected ()
	{
		return mBluetoothSocket != null && mBluetoothSocket.isConnected ();
	}

	/**
	 * @return Whether the device is currently attempting to connect.
	 */
	public boolean isConnecting ()
	{
		return connecting;
	}



	/**
	 * @param callback The callback to run on a status change.
	 */
	public void registerStatusChangeCallback ( DeviceStatusChangeCallback callback )
	{
		statusChangeCallbacks.add ( callback );
	}

	/**
	 * @param callback The callback to stop running on status change.
	 */
	public void unregisterStatusChangeCallback ( DeviceStatusChangeCallback callback )
	{
		statusChangeCallbacks.remove ( callback );
	}



	/**
	 * THIS METHOD IS BLOCKING, THEREFORE IT SHOULD BE CALLED FROM WITHIN A THREAD.
	 *
	 * Also call BluetoothAdapter#cancelDiscovery() before calling this function.
	 *
	 * @param uuid The UUID of the service to connect with.
	 * @return Whether connection succeeded.
	 */
	public boolean connect ( UUID uuid )
	{
		/* Create the socket, if this has not been done already */
		if ( mBluetoothSocket == null )
			try
			{
				mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord ( uuid );
			} catch ( IOException | SecurityException e )
			{
				connecting = false;
			}

		/* State that we are attempting to connect */
		connecting = true;
		broadcastStatusChange ();

		/* Attempt to connect */
		try
		{
			mBluetoothSocket.connect ();
		} catch ( IOException | SecurityException e )
		{
			close ();
			connecting = false;
			return false;
		}

		/* We have finished connecting */
		connecting = false;
		broadcastStatusChange ();
		return true;
	}



	/**
     * @return Whether the socket closed cleanly.
	 */
	public boolean close ()
	{
		if ( mBluetoothSocket != null )
			try
			{
				mBluetoothSocket.close ();
			} catch ( IOException e )
			{
				mBluetoothSocket = null;
				broadcastStatusChange ();
				return false;
			}
		broadcastStatusChange ();
		return true;
	}



	/**
	 * Notify all subscribers that a status change has occurred.
	 */
	private void broadcastStatusChange ()
	{
		for ( DeviceStatusChangeCallback callback : statusChangeCallbacks )
			callback.callback ( this );
	}



	/**
	 * An interface for status changes.
	 */
	public interface DeviceStatusChangeCallback
	{
		void callback ( Device device );
	}
}
