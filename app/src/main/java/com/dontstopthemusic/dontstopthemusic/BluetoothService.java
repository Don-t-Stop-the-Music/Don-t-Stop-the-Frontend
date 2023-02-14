package com.dontstopthemusic.dontstopthemusic;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.nio.channels.Channel;
import java.util.HashSet;

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



	/* The focus device (i.e. the one clicked by the user on the BluetoothActivity page) */
	private Device mFocusDevice;



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
		( ( NotificationManager ) getSystemService ( Activity.NOTIFICATION_SERVICE ) )
				.createNotificationChannel ( mNotificationChannel );

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
	 * @param device Start reading a new device.
	 */
	public void connectToDevice ( Device device )
	{
		device.connect ();
	}

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
}