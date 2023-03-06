package com.dontstopthemusic.dontstopthemusic;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/** The data format is as follows:

 {
 	"frequency": [
		0: Array 	// Uniformly distributed samples of frequency between minFrequency and maxFrequency
 		1: Array
 	],
 	"minFrequency": Int,
 	"maxFrequency": Int,
 	"hiss": [
 		0: Boolean
 		1: Boolean
 	],
 	"feedback": [
 		0: Array   // Arbitrary size, specifying which frequencies are feedbacks
 		1: Array
 	]
 }

 */


/**
 * Represents a Bluetooth device, including its socket.
 * Thread safe.
 */
public class Device implements AutoCloseable
{

	/* The bluetooth device itself */
	private final BluetoothDevice mBluetoothDevice;

	/* The socket for the device */
	private BluetoothSocket mBluetoothSocket;

	/* A reader for the socket */
	private BufferedReader mSocketReader;

	/* A connection thread */
	private Thread mConnectionThread;

	/* Whether the device is currently trying to connect */
	private boolean mConnecting = false;

	/* The most recent JSON data */
	private JSONObject mMostRecentData;

	/* A set of callbacks for status changes */
	private final Set<StatusChangeCallback> mStatusChangeCallbacks = new HashSet<> ();

	/* A set of callbacks for new data */
	private final Set<NewDataCallback> mNewDataCallbacks = new HashSet<> ();


	/* UUID */
	static final UUID RPI_UUID = new UUID ( 0x1e0ca4ea299d4335L, 0x93eb27fcfe7fa848L );


	/**
	 * @param bluetoothDevice The Bluetooth device.
	 */
	public Device ( BluetoothDevice bluetoothDevice )
	{
		/* Set the device */
		mBluetoothDevice = bluetoothDevice;
	}

	/**
	 * @param bluetoothDevice      The Bluetooth device.
	 * @param statusChangeCallback A callback to be run on status change.
	 */
	public Device ( BluetoothDevice bluetoothDevice, StatusChangeCallback statusChangeCallback )
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
	 * @return Whether the device is currently connected.
	 */
	public synchronized boolean isConnected ()
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
	 *                 The callback is also immediately run.
	 */
	public void registerStatusChangeCallback ( StatusChangeCallback callback )
	{
		synchronized ( mStatusChangeCallbacks )
		{
			mStatusChangeCallbacks.add ( callback );
			if ( mMostRecentData != null )
				callback.onStatusChange ( this );
		}
	}

	/**
	 * @param callback The callback to stop running on status change.
	 */
	public void unregisterStatusChangeCallback ( StatusChangeCallback callback )
	{
		synchronized ( mStatusChangeCallbacks )
		{
			mStatusChangeCallbacks.remove ( callback );
		}
	}

	/**
	 * @param callback The callback to run on a status change.
	 *                 The callback is also immediately run.
	 */
	public void registerNewDataCallback ( NewDataCallback callback )
	{
		synchronized ( mNewDataCallbacks )
		{
			mNewDataCallbacks.add ( callback );
			callback.onNewData ( this, mMostRecentData );
		}
	}

	/**
	 * @param callback The callback to stop running on status change.
	 */
	public void unregisterNewDataCallback ( NewDataCallback callback )
	{
		synchronized ( mNewDataCallbacks )
		{
			mNewDataCallbacks.remove ( callback );
		}
	}


	/**
	 * Call BluetoothAdapter#cancelDiscovery() before calling this function.
	 */
	public void connect ()
	{
		/* Start connecting in a new thread, if not already connected */
		if ( !isConnected () && ( mConnectionThread == null || !mConnectionThread.isAlive () ) )
			mConnectionThread = new Thread ( () ->
			{
				/* State that we are attempting to connect */
				mConnecting = true;
				broadcastStatusChange ();

				/* Attempt to connect */
				try
				{
					/* Open the socket */
					if ( mBluetoothSocket == null )
						mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord ( RPI_UUID );

					/* Connect to the socket */
					mBluetoothSocket.connect ();

					/* Create a buffered reader from the socket */
					mSocketReader = new BufferedReader ( new InputStreamReader ( mBluetoothSocket.getInputStream () ) );
				} catch ( IOException | SecurityException e )
				{
					close ();
				} finally
				{
					mConnecting = false;
					broadcastStatusChange ();
				}

				/* Return if connection failed */
				if ( !isConnected () )
					return;

				/* Infinitely read from the socket */
				try
				{
					perpetuallyRead ();
				} catch ( IOException | JSONException ignore )
				{
				} finally
				{
					close ();
				}
			} );

		/* Start the thread */
		mConnectionThread.start ();
	}


	/**
	 * Close the socket.
	 */
	@Override
	public void close ()
	{
		/* Close the socket */
		if ( mBluetoothSocket != null && mBluetoothSocket.isConnected () )
			try
			{
				mBluetoothSocket.close ();
			} catch ( IOException ignored )
			{
				mBluetoothSocket = null;
			} finally
			{
				broadcastStatusChange ();
			}
	}


	/**
	 * Notify all subscribers that a status change has occurred.
	 */
	public void broadcastStatusChange ()
	{
		synchronized ( mStatusChangeCallbacks )
		{
			for ( StatusChangeCallback callback : mStatusChangeCallbacks )
				callback.onStatusChange ( this );
		}
	}

	/**
	 * Notify all subscribers that a status change has occurred.
	 */
	public void broadcastNewData ( JSONObject data )
	{
		synchronized ( mNewDataCallbacks )
		{
			/* Set the most recent data */
			mMostRecentData = data;

			/* Run the callbacks */
			for ( NewDataCallback callback : mNewDataCallbacks )
				callback.onNewData ( this, mMostRecentData );
		}
	}


	/**
	 * Perpetually read from the input buffer.
	 */
	private void perpetuallyRead () throws IOException, JSONException
	{
		/* Loop over building strings from the buffer */
		while ( true )
		{
			/* Build up the string until a null character is found */
			StringBuilder jsonStr = new StringBuilder ();
			for ( int r = mSocketReader.read (); ; r = mSocketReader.read () )
				if ( r == 0 ) break;
				else if ( r == -1 ) return;
				else jsonStr.append ( ( char ) r );

			/* If the string is empty, continue */
			if ( jsonStr.length () == 0 ) continue;

			/* JSONify */
			broadcastNewData ( new JSONObject ( jsonStr.toString () ) );
		}
	}


	/**
	 * An interface for status changes.
	 */
	public interface StatusChangeCallback
	{
		/**
		 * @param device The device whose status has changed.
		 */
		void onStatusChange ( Device device );
	}

	/**
	 * An interface for new data.
	 */
	public interface NewDataCallback
	{
		/**
		 * @param device The device that the data has come from.
		 * @param data   The data. May be null.
		 */
		void onNewData ( Device device, @Nullable JSONObject data );
	}
}
