package com.dontstopthemusic.dontstopthemusic;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BluetoothService extends Service
{

	/* The binder for the service */
	private final LocalBinder mBinder = new LocalBinder ();

	/* The notification channel */
	private static final String CHANNEL_ID = "BluetoothChannel";
	private static final String CHANNEL_DESC = "Bluetooth foreground service notification";
	private NotificationChannel mNotificationChannel;

	/* The notification intent */
	private PendingIntent mNotificationIntent;

	/* The foreground service notification */
	private NotificationCompat.Builder mNotificationBuilder;

	/* The notification manager */
	private NotificationManager mNotificationManager;

	/* Device status change callback for modifying the notification */
	private DeviceStatusChangeCallback mDeviceStatusChangeCallback = new DeviceStatusChangeCallback ();



	/* The list of known devices */
	private final ArrayList<Device> mKnownDevices = new ArrayList<> ();

	/* A comparator for devices */
	private final DeviceComparator mDeviceComparator = new DeviceComparator ();



	/* The focus device (i.e. the one clicked by the user on the BluetoothActivity page) */
	private Device mFocusDevice = new StubDevice ();



	/* Whether the service is running */
	private static boolean mIsRunning = false;


	@Override
	public void onCreate ()
	{
		/* Create the superclass */
		super.onCreate ();

		/* Create the notification channel */
		mNotificationChannel = new NotificationChannel (
				BluetoothService.CHANNEL_ID,
				BluetoothService.CHANNEL_DESC,
				NotificationManager.IMPORTANCE_DEFAULT );

		/* Register the channel */
		mNotificationManager = ( NotificationManager ) getSystemService ( Activity.NOTIFICATION_SERVICE );
		mNotificationManager.createNotificationChannel ( mNotificationChannel );

		/* Create the notification intent */
		mNotificationIntent = PendingIntent.getActivity (
			this,
			0,
			new Intent ( this, BluetoothService.class ),
			PendingIntent.FLAG_IMMUTABLE );

		/* Create the notification builder */
		mNotificationBuilder = new NotificationCompat.Builder ( this, CHANNEL_ID )
				.setContentTitle ( getString ( R.string.bluetooth_service_notification_title ) )
				.setContentIntent ( mNotificationIntent )
				.setContentText ( getString ( R.string.bluetooth_service_notification_contents, 0 ) );

		/* Start as a foreground service */
		startForeground ( 1, mNotificationBuilder.build () );
	}

	@Override
	public int onStartCommand ( Intent intent, int flags, int startID )
	{
		/* Start the superclass */
		super.onStartCommand ( intent, flags, startID );

		/* This is not a sticky service */
		mIsRunning = true;
		return START_NOT_STICKY;
	}

	/**
	 * @param intent Ignored
	 * @return A LocalBinder instance for the service.
	 */
	@Override
	public LocalBinder onBind ( Intent intent )
	{
		return mBinder;
	}

	@Override
	public void onDestroy ()
	{
		super.onDestroy ();
		mIsRunning = false;
	}



	/**
	 * @param bluetoothDevice A new BluetoothDevice to register.
	 * @return Either new Device or an existing Device.
	 */
	public Device registerDevice ( BluetoothDevice bluetoothDevice )
	{
		/* Try to find a matching device */
		int index = IntStream.range ( 0, mKnownDevices.size () )
				.filter ( i -> mDeviceComparator.compare ( mKnownDevices.get ( i ).getInfo (), bluetoothDevice ) == 0 )
				.findFirst ()
				.orElse ( mKnownDevices.size () );

		/* Create a new device if a match has not been found */
		if ( index == mKnownDevices.size () )
		{
			Device device = new Device ( bluetoothDevice );
			mKnownDevices.add ( device );
			device.registerStatusChangeCallback ( mDeviceStatusChangeCallback );
		}

		/* Return the index of the device */
		return mKnownDevices.get ( index );
	}

	/**
	 * Remove disconnected devices from the service.
	 *
	 * @return The disconnected devices.
	 */
	public Device[] unregisterDisconnectedDevices ()
	{
		List<Device> devices = mKnownDevices
				.stream()
				.filter ( device -> !device.isConnected () && !device.isConnecting () )
				.collect( Collectors.toList() );
		for ( Device device : devices )
			device.unregisterStatusChangeCallback ( mDeviceStatusChangeCallback );
		mKnownDevices.removeAll ( devices );
		return devices.toArray ( new Device [ 0 ] );
	}

	/**
	 * @return An iterable to known devices.
	 */
	public Iterable<Device> getRegisteredDevices ()
	{
		return mKnownDevices;
	}

	/**
	 * @param device Start reading a new device.
	 */
	public void connectToDevice ( Device device ) {	device.connect ();	}

	/**
	 * @param device The device to set as the focus
	 */
	public void setFocusDevice ( Device device ) { mFocusDevice = device; }

	/**
	 * @return The device currently in focus, or null if a device is not in focus.
	 */
	public Device getFocusDevice () { return mFocusDevice; }

	/**
	 * @return Whether the process is running.
	 */
	public static boolean isRunning ()
	{
		return mIsRunning;
	}



	/**
	 * A binder which returns a Bluetooth service instance.
	 */
	public class LocalBinder extends Binder
	{
		BluetoothService getService ()
		{
			return BluetoothService.this;
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



	/**
	 * Implementation of Device.DeviceStatusChangeCallback for updating the notification on device changes.
	 */
	private class DeviceStatusChangeCallback implements Device.StatusChangeCallback
	{
		@Override
		public void onStatusChange ( Device device )
		{
			int connected = mKnownDevices.stream().reduce ( 0, ( Integer u, Device d ) -> d.isConnected () ? u + 1 : u, Integer::sum );
			mNotificationBuilder.setContentText ( getString ( R.string.bluetooth_service_notification_contents, 0 ) );
			mNotificationManager.notify ( 1, mNotificationBuilder.build () );
		}
	}
}