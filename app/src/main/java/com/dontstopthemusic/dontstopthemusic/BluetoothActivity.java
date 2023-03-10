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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;

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

	/* Whether bluetooth is currently scanning */
	boolean mScanning;



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

		/* Start the bluetooth service */
		startForegroundService ( new Intent ( this, BluetoothService.class ) );




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
				startActivity ( new Intent ( BluetoothActivity.this, MainActivity.class ) );
			}
		} );

		/* Create a click scan callback */
		mScanButton.setOnClickListener ( view ->
		{
			if ( !mScanning )
			{
				unregisterDisconnectedDevices ();
				scanForDevices ( true );
			}
		} );


	}

	@Override
	protected void onStart ()
	{
		/* Start the superclass */
		super.onStart ();

		/* Bind to the Bluetooth service */
		bindService (
				new Intent ( this, BluetoothService.class ),
				mBluetoothServiceConnection,
				Context.BIND_AUTO_CREATE );

		/* Register the bluetooth receiver */
		IntentFilter filter = new IntentFilter ( BluetoothDevice.ACTION_FOUND );
		filter.addAction ( BluetoothAdapter.ACTION_DISCOVERY_FINISHED );
		registerReceiver ( mBluetoothReceiver, filter );
	}

	/**
	 * When the activity is no longer visible
	 */
	@Override
	protected void onStop ()
	{
		/* Stop the superclass */
		super.onStop ();

		/* Stop scanning */
		scanForDevices ( false );
		unregisterReceiver ( mBluetoothReceiver );

		/* Unbind the Bluetooth service */
		unbindService ( mBluetoothServiceConnection );
		mBluetoothServiceConnection.onServiceDisconnected ( null );
	}

	/**
	 * Run once when the activity is destroyed.
	 */
	@Override
	protected void onDestroy ()
	{
		/* Destroy the superclass */
		super.onDestroy ();
	}



	/**
	 * @param bluetoothDevice A new BluetoothDevice to register.
	 * @return Either new Device or an existing Device.
	 */
	private Device registerDevice ( BluetoothDevice bluetoothDevice )
	{
		Device device = mBluetoothService.registerDevice ( bluetoothDevice );
		device.registerStatusChangeCallback ( mDeviceStatusChangeCallback );
		return device;
	}

	/**
	 * Forget about all currently disconnected devices.
	 */
	private void unregisterDisconnectedDevices ()
	{
		Device[] devices = mBluetoothService.unregisterDisconnectedDevices ();
		mOtherDevicesFragment.removeDevices ( Arrays.asList ( devices ) );
		for ( Device device : devices )
			device.unregisterStatusChangeCallback ( mDeviceStatusChangeCallback );
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

				/* Fade the button */
				mScanButton.setText ( getString( R.string.scanning ) );
				mScanButton.setClickable ( false );
				mScanButton.setAlpha ( 0.5f );
			}

			else if ( !enable && mScanning )
			{
				/* Stop scanning */
				mScanning = false;
				mBluetoothAdapter.cancelDiscovery ();

				/* Show the button */
				mScanButton.setText ( getString( R.string.scan ) );
				mScanButton.setClickable ( true );
				mScanButton.setAlpha ( 1 );
			}
	}



	/**
	 * @return True iff permissions have already been granted.
	 */
	private boolean checkBluetoothPermissions ()
	{
		boolean granted = true;
		for ( String p : requiredBluetoothPermissions ()  )
			granted &= ActivityCompat.checkSelfPermission ( this, p ) == PackageManager.PERMISSION_GRANTED;
		return granted;
	}

	/**
	 * @return True iff the required Bluetooth permissions have been granted.
	 */
	private boolean checkBluetoothEnabled ()
	{
		boolean enabled = mBluetoothAdapter.isEnabled ();
		if ( !enabled )
			Toast.makeText ( this, R.string.enable_bluetooth, Toast.LENGTH_SHORT ).show ();
		return enabled;
	}

	/**
	 * Request the required Bluetooth permissions
	 */
	private void requestBluetoothPermissions ()
	{
		ActivityCompat.requestPermissions ( BluetoothActivity.this, requiredBluetoothPermissions (), 2 );
	}

	/**
	 * Get Bluetooth permissions based on SDK version
	 */
	private String[] requiredBluetoothPermissions ()
	{
		ArrayList<String> permissions = new ArrayList<> ();
		permissions.add ( Manifest.permission.ACCESS_FINE_LOCATION );
		permissions.add ( Manifest.permission.ACCESS_COARSE_LOCATION );
		if ( Build.VERSION.SDK_INT >= 31 )
		{
			permissions.add ( Manifest.permission.ACCESS_FINE_LOCATION );
			permissions.add ( Manifest.permission.BLUETOOTH_CONNECT );
		}
		if ( Build.VERSION.SDK_INT >= 29 )
		{
			permissions.add ( Manifest.permission.ACCESS_BACKGROUND_LOCATION );
		}
		return permissions.toArray ( new String [ 0 ] );
	};



	/**
	 * Allows for binding to the Bluetooth service.
	 */
	public class BluetoothServiceConnection implements ServiceConnection
	{
		/**
		 * @param component Unused
		 */
		@Override
		public void onServiceDisconnected ( @Nullable ComponentName component )
		{
			/* Theres a small race condition meaning that we may not have actually connected */
			if ( mBluetoothService != null )
			{
				/* Remove the callbacks */
				for ( Device device : mBluetoothService.getRegisteredDevices () )
					device.unregisterStatusChangeCallback ( mDeviceStatusChangeCallback );

				/* Nullify the connection */
				mBluetoothService = null;
			}
		}

		/**
		 * @param component Unused
		 * @param service The service binder (actually an instance of BluetoothService.LocalBinder)
		 */
		@Override
		public void onServiceConnected ( ComponentName component, IBinder service )
		{
			/* Bind */
			BluetoothService.LocalBinder binder = ( BluetoothService.LocalBinder ) service;
			mBluetoothService = binder.getService ();

			/* Load the devices from the service */
			for ( Device device : mBluetoothService.getRegisteredDevices () )
				device.registerStatusChangeCallback ( mDeviceStatusChangeCallback );

			/* Refresh the fragments */
			mConnectedDevicesFragment.refreshDevices ();
			mOtherDevicesFragment.refreshDevices ();

			/* Begin scanning */
			scanForDevices ( true );
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
				scanForDevices ( false );
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


}
