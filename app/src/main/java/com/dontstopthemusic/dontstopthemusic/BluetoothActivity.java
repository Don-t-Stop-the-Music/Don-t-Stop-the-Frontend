package com.dontstopthemusic.dontstopthemusic;

import static android.util.Log.println;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.security.Permission;

public class BluetoothActivity extends AppCompatActivity
{
	/* The bluetooth adapter */
	private BluetoothAdapter mBluetoothAdapter;

	/* The current device fragment */
	private DeviceListFragment currentDeviceFragment;

	/* The list of other devices */
	private DeviceListFragment otherDevicesFragment;

	/* The bluetooth device receiver */
	private BluetoothDeviceReceiver mBluetoothReceiver = new BluetoothDeviceReceiver ();

	/* Whether bluetooth is currently scanning */
	boolean mScanning;



	/* Required permissions */
	static final String[] requiredPermissions =
		{
				Manifest.permission.BLUETOOTH_CONNECT,
				Manifest.permission.BLUETOOTH_SCAN,
				Manifest.permission.ACCESS_FINE_LOCATION,
				//Manifest.permission.ACCESS_COARSE_LOCATION,
				//Manifest.permission.ACCESS_BACKGROUND_LOCATION
		};



	/**
	 * @param savedInstanceState If the fragment is being re-created from
	 *                           a previous saved state, this is the state.
	 */
	@Override
	public void onCreate ( Bundle savedInstanceState )
	{
		/* Create the superclass */
		super.onCreate ( savedInstanceState );

		/* Set up the content view */
		setContentView ( R.layout.activity_bluetooth );

		/* Get copies of the fragments */
		currentDeviceFragment = ( DeviceListFragment ) getSupportFragmentManager ().findFragmentById ( R.id.currentDevice );

		/* Get copies of the fragments */
		otherDevicesFragment = ( DeviceListFragment ) getSupportFragmentManager ().findFragmentById ( R.id.otherDevices );

		/* Check that classic bluetooth is supported on the device */
		if ( !getPackageManager ().hasSystemFeature ( PackageManager.FEATURE_BLUETOOTH ) )
		{
			Toast.makeText ( this, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT ).show ();
			finish ();
			return;
		}

		/* Check permissions */
		if ( !checkBluetoothPermissions () )
			requestBluetoothPermissions ();

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

		/* Clear the list of other devices */
		otherDevicesFragment.clearDevices ();

		/* Ensure Bluetooth is enabled on the device.
		 * If Bluetooth is not currently enabled, fire an intent to display a dialog asking the user to grant permission to enable it.
		 */
		if ( !mBluetoothAdapter.isEnabled () )
		{
			throw new SecurityException ( "Bluetooth is disabled and the a request is not yet implemented!" );
			//Intent enableBtIntent = new Intent ( BluetoothAdapter.ACTION_REQUEST_ENABLE );
			//startActivityForResult ( enableBtIntent, 1 );
		}

		/* Check permissions */
		if ( !checkBluetoothPermissions () )
			requestBluetoothPermissions ();

		/* Scan! */
		scanForDevices ( true );
	}



	/**
	 * @param enable Whether to enable or disable bluetooth scanning
	 */
	private void scanForDevices ( final boolean enable ) throws SecurityException
	{
		if ( checkBluetoothPermissions () )
			if ( enable && !mScanning )
			{
				/* Register the Bluetooth receiver */
				IntentFilter filter = new IntentFilter ( BluetoothDevice.ACTION_FOUND );
				registerReceiver ( mBluetoothReceiver, filter );

				/* Start scanning */
				mScanning = true;
				mBluetoothAdapter.startDiscovery ();
			}

			else if ( !enable && mScanning )
			{
				/* Stop scanning */
				mScanning = false;
				mBluetoothAdapter.cancelDiscovery ();

				/* Unregister the receiver */
				unregisterReceiver ( mBluetoothReceiver );
			}
	}


	/**
	 * @return True iff permissions have already been granted.
	 */
	private boolean checkBluetoothPermissions ()
	{
		boolean granted = true;
		for ( String p : requiredPermissions  )
			granted &= ActivityCompat.checkSelfPermission ( this, p ) == PackageManager.PERMISSION_GRANTED;
		return granted;
	}

	/**
	 *
	 */
	private void requestBluetoothPermissions ()
	{
		ActivityCompat.requestPermissions ( BluetoothActivity.this, requiredPermissions, 2 );
	}



	/* A BroadcastReceiver for new Bluetooth devices */
	private class BluetoothDeviceReceiver extends BroadcastReceiver
	{
		/* Receiver method */
		@Override
		public void onReceive ( Context context, Intent intent )
		{
			/* Check that this is actually a new device broadcast */
			if ( BluetoothDevice.ACTION_FOUND.equals ( intent.getAction () ) )
			{
				BluetoothDevice device = intent.getParcelableExtra ( BluetoothDevice.EXTRA_DEVICE );
				if ( filterDevice ( device ) )
					otherDevicesFragment.addDevice ( device );
			}
		}

		/* Filter out certain devices */
		private boolean filterDevice ( BluetoothDevice device ) throws SecurityException
		{
			return device.getName () != null;
		}
	};
};
