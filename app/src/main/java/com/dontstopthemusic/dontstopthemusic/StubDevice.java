package com.dontstopthemusic.dontstopthemusic;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import org.json.JSONObject;

/**
 * A pretend device for testing purposes.
 */
public class StubDevice extends Device
{
	/** Whether the device is 'connected' */
	private boolean mConnected = true;



	/**
	 */
	public StubDevice ()
	{
		/* Create the superclass with a null device */
		super ( null );

		/* Start the writing thread */
		new Thread ( () ->
		{
			while ( mConnected )
			{
				try
				{
					Thread.sleep ( 200 );
				} catch ( InterruptedException ignored )
				{
				} finally
				{
					broadcastNewData ( new JSONObject () );
				}
			}
		} ).start ();
	}

	/**
	 * @return Whether the device is connected
	 */
	@Override
	public boolean isConnected ()
	{
		return mConnected;
	}

	/**
	 * @return False
	 */
	@Override
	public boolean isConnecting ()
	{
		return false;
	}

	/**
	 * Does nothing
	 */
	@Override
	public void connect ()
	{
	}

	/**
	 *
	 */
	@Override
	public void close ()
	{
		mConnected = false;
		broadcastStatusChange ();
	}
}
