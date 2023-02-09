package com.dontstopthemusic.dontstopthemusic;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class Device implements Closeable
{

	/* The bluetooth device itself */
	private final BluetoothDevice mBluetoothDevice;

	/* The socket for the device */
	private BluetoothSocket mBluetoothSocket;

	/* Whether the device is currently trying to connect */
	private boolean mConnecting = false;

	/* A set of callbacks for status changes */
	private final HashSet<DeviceStatusChangeCallback> mStatusChangeCallbacks = new HashSet<> ();



	/**
	 * @param bluetoothDevice The Bluetooth device.
	 */
	public Device ( BluetoothDevice bluetoothDevice )
	{
		/* Set the device */
		mBluetoothDevice = bluetoothDevice;
	}

	/**
	 * @param bluetoothDevice The Bluetooth device.
	 * @param statusChangeCallback A callback to be run on status change.
	 */
	public Device ( BluetoothDevice bluetoothDevice, DeviceStatusChangeCallback statusChangeCallback )
	{
		/* Set the device and callback */
		mBluetoothDevice = bluetoothDevice;
		registerStatusChangeCallback ( statusChangeCallback );
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
		return mConnecting;
	}



	/**
	 * @param callback The callback to run on a status change.
	 * The callback is also immediately run.
	 */
	public void registerStatusChangeCallback ( DeviceStatusChangeCallback callback )
	{
		mStatusChangeCallbacks.add ( callback );
		callback.callback ( this );
	}

	/**
	 * @param callback The callback to stop running on status change.
	 */
	public void unregisterStatusChangeCallback ( DeviceStatusChangeCallback callback )
	{
		mStatusChangeCallbacks.remove ( callback );
	}



	/**
	 * Call BluetoothAdapter#cancelDiscovery() before calling this function.
	 *
	 * @param uuid The UUID of the service to connect with.
	 */
	public void connect ( UUID uuid )
	{
		/* State that we are attempting to connect */
		mConnecting = true;
		broadcastStatusChange ();

		/* Create the socket, if this has not been done already */
		if ( mBluetoothSocket == null )
			try
			{
				mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord ( uuid );
			} catch ( IOException | SecurityException e )
			{
				mConnecting = false;
				broadcastStatusChange ();
				return;
			}

		/* Start connecting in a new thread */
		new Thread ( () ->
		{
			/* Attempt to connect */
			try
			{
				mBluetoothSocket.connect ();
			} catch ( IOException | SecurityException e )
			{
				close ();
			} finally
			{
				mConnecting = false;
				broadcastStatusChange ();
			}
		} ).start ();
	}



	/**
	 * Close the socket.
	 */
	@Override
	public void close ()
	{
		if ( mBluetoothSocket != null && mBluetoothSocket.isConnected () )
			try
			{
				mBluetoothSocket.close ();
			} catch ( IOException ignored )
			{
			} finally
			{
				mBluetoothSocket = null;
				broadcastStatusChange ();
			}
	}



	/**
	 * Notify all subscribers that a status change has occurred.
	 */
	private void broadcastStatusChange ()
	{
		for ( DeviceStatusChangeCallback callback : mStatusChangeCallbacks )
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
