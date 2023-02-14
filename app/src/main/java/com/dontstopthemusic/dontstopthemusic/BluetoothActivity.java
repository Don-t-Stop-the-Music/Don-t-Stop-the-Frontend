package com.dontstopthemusic.dontstopthemusic;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.IntStream;

public class BluetoothActivity extends AppCompatActivity
{
	/* The bluetooth adapter */
	private BluetoothAdapter mBluetoothAdapter;

	/* The Bluetooth service */
	private BluetoothService mBluetoothService;

	/* The current device fragment */
	private StatusDeviceListFragment mConnectedDevicesFragment;

	/* The list of other devices */
	private SimpleDeviceListFragment mOtherDevicesFragment;

	/* The scan button */
	private Button mScanButton;

	/* The bluetooth device receiver */
	private final BluetoothDeviceReceiver mBluetoothReceiver = new BluetoothDeviceReceiver ();

	/* A callback for device status changes */
	private final DeviceStatusChangeCallback mDeviceStatusChangeCallback = new DeviceStatusChangeCallback ();

	/* The connection handler for binding to the Bluetooth service */
	private final BluetoothServiceConnection mBluetoothServiceConnection = new BluetoothServiceConnection ();

	/* The list of known devices */
	private final ArrayList<Device> mKnownDevices = new ArrayList<> ();

	/* A comparator for devices */
	private final DeviceComparator mDeviceComparator = new DeviceComparator ();

	/* Whether bluetooth is currently scanning */
	boolean mScanning;



	/* Required permissions */
	static final String[] REQUIRED_PERMISSIONS =
		{
				//Manifest.permission.BLUETOOTH_CONNECT,
				//Manifest.permission.BLUETOOTH_SCAN,
				Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.ACCESS_COARSE_LOCATION,
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



		/* BLUETOOTH */

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

		/* Register the Bluetooth receiver */
		IntentFilter filter = new IntentFilter ( BluetoothDevice.ACTION_FOUND );
		filter.addAction ( BluetoothAdapter.ACTION_DISCOVERY_FINISHED );
		registerReceiver ( mBluetoothReceiver, filter );

		/* Start the bluetooth service */
		startForegroundService ( new Intent ( this, BluetoothService.class ) );

		/* Bind to the Bluetooth service */
		bindService (
				new Intent ( this, BluetoothService.class ),
				mBluetoothServiceConnection,
				Context.BIND_AUTO_CREATE );




		/* SET UP FRAGMENTS AND VIEWS */

		/* Get copies of the fragments */
		mConnectedDevicesFragment = ( StatusDeviceListFragment ) getSupportFragmentManager ().findFragmentById ( R.id.currentDevice );

		/* Get copies of the fragments */
		mOtherDevicesFragment = ( SimpleDeviceListFragment ) getSupportFragmentManager ().findFragmentById ( R.id.otherDevices );

		/* Get the scan button */
		mScanButton = ( Button ) findViewById ( R.id.scan_button );

		/* Create a callback for clicking on a non-connected device */
		mOtherDevicesFragment.setDeviceClickCallback ( device ->
		{
			scanForDevices ( false );
			mBluetoothService.connectToDevice ( device );
		} );

		/* Create a callback for clicking on a connected device */
		mConnectedDevicesFragment.setDeviceClickCallback ( device ->
		{
			if ( device.isConnected () )
			{
				scanForDevices ( false );
				mBluetoothService.setFocusDevice ( device );
				startActivity ( new Intent ( BluetoothActivity.this, BluetoothActivity.class ) );
			}
		} );

		/* Create a click scan callback */
		mScanButton.setOnClickListener ( view ->
		{
			if ( !mScanning )
			{
				clearDisconnectedDevices ();
				scanForDevices ( true );
			}
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

		/* Check permissions */
		if ( !checkBluetoothPermissions () )
			requestBluetoothPermissions ();

		/* Clear the list of other devices */
		mOtherDevicesFragment.clearDevices ();

		/* Scan */
		scanForDevices ( true );
	}

	/**
	 * Run once when the activity is destroyed.
	 */
	@Override
	protected void onDestroy ()
	{
		/* Destroy the superclass */
		super.onDestroy ();

		/* Unbind the Bluetooth service */
		unbindService ( mBluetoothServiceConnection );
	}

	/**
	 * @param bluetoothDevice A new BluetoothDevice to register.
	 * @return Either new Device or an existing Device.
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
				mConnectedDevicesFragment.removeDevice ( device );
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
				/* Start scanning */
				mScanning = true;
				mBluetoothAdapter.startDiscovery ();
			}

			else if ( !enable && mScanning )
			{
				/* Stop scanning */
				mScanning = false;
				mBluetoothAdapter.cancelDiscovery ();
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
	 * Allows for binding to the Bluetooth service.
	 */
	public class BluetoothServiceConnection implements ServiceConnection
	{
		@Override
		public void onServiceDisconnected ( ComponentName component ) {
			mBluetoothService = null;
		}

		/**
		 * @param component Unused
		 * @param service The service binder (actually an instance of BluetoothService.LocalBinder)
		 */
		@Override
		public void onServiceConnected ( ComponentName component, IBinder service )
		{
			BluetoothService.LocalBinder binder = ( BluetoothService.LocalBinder ) service;
			mBluetoothService = binder.getService();
		}
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
			/* If this is a new device broadcast */
			if ( BluetoothDevice.ACTION_FOUND.equals ( intent.getAction () ) )
			{
				BluetoothDevice bluetoothDevice = intent.getParcelableExtra ( BluetoothDevice.EXTRA_DEVICE );
				if ( filterDevice ( bluetoothDevice ) )
					registerDevice ( bluetoothDevice );
			}

			/* Otherwise if the scan has ended */
			else if ( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals ( intent.getAction () ) )
			{
				mScanning = false;
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
	private class DeviceStatusChangeCallback implements Device.StatusChangeCallback
	{
		@Override
		public void onStatusChange ( Device device )
		{
			runOnUiThread ( () ->
			{
				if ( device.isConnecting () || device.isConnected () )
				{
					mOtherDevicesFragment.removeDevice ( device );
					mConnectedDevicesFragment.addDevice ( device );
				} else
				{
					mOtherDevicesFragment.addDevice ( device );
					mConnectedDevicesFragment.removeDevice ( device );
				}
			} );
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
