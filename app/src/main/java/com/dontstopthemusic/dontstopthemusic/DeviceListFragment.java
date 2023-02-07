package com.dontstopthemusic.dontstopthemusic;

import android.Manifest;
import android.app.admin.DeviceAdminService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.ListFragment;

import java.util.ArrayList;
import java.util.Iterator;

public class DeviceListFragment extends ListFragment
{

	/* An instance of the list adapter */
	private DeviceListAdapter mDeviceListAdapter;


	/**
	 *
	 */
	@Override
	public void onResume ()
	{
		/* Resume the superclass */
		super.onResume ();

		/* Initializes the list view adapter. */
		mDeviceListAdapter = new DeviceListAdapter ();
		setListAdapter ( mDeviceListAdapter );
	}



	/**
	 * @param device Add/update the entry for this device.
	 */
	public void addDevice ( BluetoothDevice device )
	{
		mDeviceListAdapter.addDevice ( device );
        mDeviceListAdapter.notifyDataSetChanged ();
	}

    /**
     * @param devices Add/update the entry for many devices.
     */
    public void addDevices ( Iterable<BluetoothDevice> devices )
    {
        for ( BluetoothDevice device : devices )
            mDeviceListAdapter.addDevice ( device );
        mDeviceListAdapter.notifyDataSetChanged ();
    }

    /**
     * @return The number of devices in the list.
     */
    public int getCount ()
    {
        return mDeviceListAdapter.getCount ();
    }

    /**
     * Remove all devices from the list.
     */
    public void clear ()
    {
        mDeviceListAdapter.clear ();
        mDeviceListAdapter.notifyDataSetChanged ();
    }

    /**
     * @param i The ith device
     * @return A device.
     */
    public BluetoothDevice getDevice ( int i )
    {
        return ( BluetoothDevice ) mDeviceListAdapter.getItem ( i );
    }



	/**
	 * The list adapter for the fragment
	 */
	private class DeviceListAdapter extends BaseAdapter implements Iterable<BluetoothDevice>
	{

		/* A list of bluetooth devices in the adapter */
		private final ArrayList<BluetoothDevice> mDevices;

		/* An inflator for the fragment */
		private final LayoutInflater mInflator;

		/**
		 * Default constructor for the DeviceListAdapter
		 */
		public DeviceListAdapter ()
		{
			/* Construct the superclass */
			super ();

			/* Initialise members */
			mDevices = new ArrayList<> ();
			mInflator = DeviceListFragment.this.getLayoutInflater ();
		}

        @NonNull
        @Override
        public Iterator<BluetoothDevice> iterator ()
        {
            return mDevices.iterator ();
        }

        /**
		 * @param device The device to add/update in the list.
		 */
		public void addDevice ( BluetoothDevice device )
		{
			if ( !mDevices.contains ( device ) )
			{
				mDevices.add ( device );
			}
		}

		/**
		 * @return The number of devices in the adapter.
		 */
		@Override
		public int getCount ()
		{
			return mDevices.size ();
		}

		/**
		 * Remove all devices from the adapter.
		 */
		public void clear ()
		{
			mDevices.clear ();
		}

		/**
		 * @param i The index of the device to return.
		 * @return A device.
		 */
		@Override
		public Object getItem ( int i )
		{
			return mDevices.get ( i );
		}

		/**
		 * @param i The index of a device.
		 * @return The hash of the device.
		 */
		@Override
		public long getItemId ( int i )
		{
			return mDevices.get ( i ).hashCode ();
		}

        /**
         * @param i The index of a device.
         * @param view The view to update, or otherwise create if is null.
         * @param viewGroup Unused.
         * @return A new or updated view for the specified device.
         */
		@Override
		public View getView ( int i, @Nullable View view, ViewGroup viewGroup )
		{
			ViewParams viewParams;

			/* Create a new view if it does not exist already */
			if ( view == null )
			{
				view = mInflator.inflate ( R.layout.listitem_device, null );
				viewParams = new ViewParams (
						view.findViewById ( R.id.device_name ),
						view.findViewById ( R.id.device_address ) );
				view.setTag ( viewParams );
			} else
				/* Else get the existing view */
				viewParams = ( ViewParams ) view.getTag ();

			/* Update the view */
			viewParams.updateText ( mDevices.get ( i ) );

			return view;
		}
	}


	/**
	 * A class to store the modifiable parts of a view.
	 */
	static private class ViewParams
	{
		TextView deviceName, deviceAddress;

		/**
		 * Default constructor
		 *
		 * @param deviceName    The TextView that stores the device's name.
		 * @param deviceAddress The TextView that stores the device's address.
		 */
		public ViewParams ( TextView deviceName, TextView deviceAddress )
		{
			this.deviceName = deviceName;
			this.deviceAddress = deviceAddress;
		}

		/**
		 * Update the text of a list view with the parameters of a given device
		 *
		 * @param device The device with which to update the view's name and address
		 */
		public void updateText ( BluetoothDevice device ) throws SecurityException
		{
			final String name = device.getName ();
			if ( name != null && name.length () > 0 )
				this.deviceName.setText ( name );
			else
				this.deviceName.setText ( R.string.unknown_device );
			this.deviceAddress.setText ( device.getAddress () );
		}
	}
}
