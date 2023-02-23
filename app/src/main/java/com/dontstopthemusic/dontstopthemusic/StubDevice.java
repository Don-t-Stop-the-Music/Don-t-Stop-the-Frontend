package com.dontstopthemusic.dontstopthemusic;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import org.json.JSONArray;
import org.json.JSONException;
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

		/* Create the stub data */
		JSONObject stubData = new JSONObject ();

		/* Try to populate */
		try
		{
			/* Set the min and max frequency */
			final int minFrequency = 0, maxFrequency = 22050;
			stubData.put ( "minFrequency", minFrequency );
			stubData.put ( "maxFrequency", maxFrequency );

			/* Create the frequency data */
			JSONArray freqData = new JSONArray ();
			stubData.put ( "frequency", freqData );
			JSONArray freqData1 = new JSONArray ();
			JSONArray freqData2 = new JSONArray ();
			freqData.put ( freqData1 );
			freqData.put ( freqData2 );
			for ( int i = 0; i < 160; ++i )
			{
				freqData1.put ( Math.pow ( Math.sin ( i / Math.PI ), 2 ) );
				freqData1.put ( Math.pow ( Math.cos ( i / ( 2 * Math.PI ) ), 2 ) );
			}

			/* Set hiss */
			JSONArray hissData = new JSONArray ();
			stubData.put ( "hiss", hissData );
			hissData.put ( false );
			hissData.put ( false );

			/* Set feedback */
			stubData.put ( "feedback", new JSONObject () );
		} catch ( JSONException ignore ) {}

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
					broadcastNewData ( stubData );
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
