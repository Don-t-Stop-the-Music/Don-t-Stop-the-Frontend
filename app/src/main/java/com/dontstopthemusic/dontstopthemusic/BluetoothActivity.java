package com.dontstopthemusic.dontstopthemusic;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class BluetoothActivity extends Activity
{

	/* Allows for scheduling of events */
	Handler mHandler;

	/* The bluetooth adapter */
	BluetoothAdapter mBluetoothAdapter;

	/**
	 * @param savedInstanceState If the fragment is being re-created from
	 *                           a previous saved state, this is the state.
	 */
	@Override
	public void onCreate ( Bundle savedInstanceState )
	{
		super.onCreate ( savedInstanceState );
		mHandler = new Handler ();

		/* Check that classic bluetooth is supported on the device */
		if ( !getPackageManager ().hasSystemFeature ( PackageManager.FEATURE_BLUETOOTH ) )
		{
			Toast.makeText ( this, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT ).show ();
			finish ();
			return;
		}

		/* Check permissions */
		if ( ActivityCompat.checkSelfPermission ( this, Manifest.permission.BLUETOOTH_CONNECT ) != PackageManager.PERMISSION_GRANTED )
		{
			Toast.makeText ( this, R.string.no_bluetooth_permissions, Toast.LENGTH_SHORT ).show ();
			finish ();
			return;
		}

		/* Initializes the Bluetooth adapter. For API level 18 and above, get a reference to
		 * BluetoothAdapter through BluetoothManager.
		 */
		final BluetoothManager bluetoothManager =
				( BluetoothManager ) getSystemService ( Context.BLUETOOTH_SERVICE );
		mBluetoothAdapter = bluetoothManager.getAdapter ();

		/* Check again if bluetooth is supported */
		if ( mBluetoothAdapter == null )
		{
			Toast.makeText ( this, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT ).show ();
			finish ();
			return;
		}
	}



	/**
	 * Run when the activity resumes, including after onStart.
	 */
	@Override
	protected void onResume () throws SecurityException
	{
		/* Resume the superclass */
		super.onResume ();

		/* Ensure Bluetooth is enabled on the device.
		 * If Bluetooth is not currently enabled, fire an intent to display a dialog asking the user to grant permission to enable it.
		 */
		if ( !mBluetoothAdapter.isEnabled () )
		{
			Intent enableBtIntent = new Intent ( BluetoothAdapter.ACTION_REQUEST_ENABLE );
			startActivityForResult ( enableBtIntent, 1 );
		}
	}

}
