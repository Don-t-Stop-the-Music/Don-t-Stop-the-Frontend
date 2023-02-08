package com.dontstopthemusic.dontstopthemusic;

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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.IntStream;

public class BluetoothActivity extends AppCompatActivity
{
	/* The bluetooth adapter */
	private BluetoothAdapter mBluetoothAdapter;

	/* The current device fragment */
	private SimpleDeviceListFragment mCurrentDevicesFragment;

	/* The list of other devices */
	private SimpleDeviceListFragment mOtherDevicesFragment;

	/* The bluetooth device receiver */
	private final BluetoothDeviceReceiver mBluetoothReceiver = new BluetoothDeviceReceiver ();

	private final DeviceStatusChangeCallback mDeviceStatusChangeCallback = new DeviceStatusChangeCallback ();

	/* The list of known devices */
	private final ArrayList<Device> mKnownDevices = new ArrayList<> ();

	/* A comparator for devices */
	private final DeviceComparator mDeviceComparator = new DeviceComparator ();

	/* Whether bluetooth is currently scanning */
	boolean mScanning;



	static final UUID BASE_UUID = UUID.fromString ( "00000000-0000-1000-8000-00805F9B34F" );

	/* Required permissions */
	static final String[] REQUIRED_PERMISSIONS =
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

		/* Get copies of the fragments */
		mCurrentDevicesFragment = ( SimpleDeviceListFragment ) getSupportFragmentManager ().findFragmentById ( R.id.currentDevice );

		/* Get copies of the fragments */
		mOtherDevicesFragment = ( SimpleDeviceListFragment ) getSupportFragmentManager ().findFragmentById ( R.id.otherDevices );

		/* Create a callback for clicking on a non-connected device */
		mOtherDevicesFragment.setDeviceClickCallback ( device ->
		{
			scanForDevices ( false );
			device.connect ( BASE_UUID );
		} );
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
		mOtherDevicesFragment.clearDevices ();

		/* Scan! */
		scanForDevices ( true );
	}



	/**
	 * @param bluetoothDevice A new BluetoothDevice to register.
	 * @return Either new Device or an existing mathing Device.
	 */
	private Device registerDevice ( BluetoothDevice bluetoothDevice )
	{
		/* Try to find a matching device */
		int index = IntStream.range ( 0, mKnownDevices.size () )
				.filter ( i -> mDeviceComparator.compare ( mKnownDevices.get ( i ).getInfo (), bluetoothDevice ) == 0 )
				.findFirst ()
				.orElse ( mKnownDevices.size () );

		/* Create a new device if a match has not been found */
		if ( index == mKnownDevices.size () )
			mKnownDevices.add ( new Device ( bluetoothDevice, mDeviceStatusChangeCallback ) );

		/* Return the index of the device */
		return mKnownDevices.get ( index );
	}

	private void clearDisconnectedDevices ()
	{
		mKnownDevices.removeIf ( device ->
		{
			if ( !device.isConnected () && !device.isConnecting () )
			{
				device.unregisterStatusChangeCallback ( mDeviceStatusChangeCallback );
				mCurrentDevicesFragment.removeDevice ( device );
				mOtherDevicesFragment.removeDevice ( device );
				return true;
			} else return false;
		} );
	}



	/**
	 * @param enable Whether to enable or disable bluetooth scanning
	 */
	private void scanForDevices ( final boolean enable ) throws SecurityException
	{
		if ( checkBluetoothPermissions () && checkBluetoothEnabled () )
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
		for ( String p : REQUIRED_PERMISSIONS  )
			granted &= ActivityCompat.checkSelfPermission ( this, p ) == PackageManager.PERMISSION_GRANTED;
		return granted;
	}

	private boolean checkBluetoothEnabled ()
	{
		boolean enabled = mBluetoothAdapter.isEnabled ();
		if ( !enabled )
			Toast.makeText ( this, R.string.enable_bluetooth, Toast.LENGTH_SHORT ).show ();
		return enabled;
	}

	/**
	 *
	 */
	private void requestBluetoothPermissions ()
	{
		ActivityCompat.requestPermissions ( BluetoothActivity.this, REQUIRED_PERMISSIONS, 2 );
	}



	/**
	 * A BroadcastReceiver for new Bluetooth devices
	 */
	private class BluetoothDeviceReceiver extends BroadcastReceiver
	{
		/* Receiver method */
		@Override
		public void onReceive ( Context context, Intent intent )
		{
			/* Check that this is actually a new device broadcast */
			if ( BluetoothDevice.ACTION_FOUND.equals ( intent.getAction () ) )
			{
				BluetoothDevice bluetoothDevice = intent.getParcelableExtra ( BluetoothDevice.EXTRA_DEVICE );
				if ( filterDevice ( bluetoothDevice ) )
					mOtherDevicesFragment.addDevice ( new Device ( bluetoothDevice ) );
			}
		}

		/* Filter out certain devices */
		private boolean filterDevice ( BluetoothDevice device ) throws SecurityException
		{
			return device != null && device.getName () != null;
		}
	}



	/**
	 * Implementation of Device.DeviceStatusChangeCallback for handling device status changes.
	 */
	private class DeviceStatusChangeCallback implements Device.DeviceStatusChangeCallback
	{
		@Override
		public void callback ( Device device )
		{
			if ( device.isConnecting () || device.isConnected () )
			{
				mOtherDevicesFragment.removeDevice ( device );
				mCurrentDevicesFragment.addDevice ( device );
			} else
			{
				mOtherDevicesFragment.addDevice ( device );
				mCurrentDevicesFragment.removeDevice ( device );
			}
		}
	}


	/**
	 * A comparator over MAC addresses of devices.
	 */
	static private class DeviceComparator implements Comparator<BluetoothDevice>
	{
		/**
		 * @param d1 The first device
		 * @param d2 The second device
		 * @return The comparison of the MAC addresses of devices.
		 */
		@Override
		public int compare ( BluetoothDevice d1, BluetoothDevice d2 )
		{
			long cmp = addressToInt ( d1.getAddress () ) - addressToInt ( d2.getAddress () );
			return cmp < 0 ? -1 : ( cmp > 0 ? 1 : 0 );
		}

		/**
		 * @param address A MAC address of the form AA:BB:CC:DD:EE:FF
		 * @return An integer representation.
		 */
		private long addressToInt ( String address )
		{
			long result = 0;
			String[] bytes = address.split ( ":" );
			for ( String b : bytes )
				result = result * 16 + Long.parseLong ( b, 16 );
			return result;
		}
	}
}
