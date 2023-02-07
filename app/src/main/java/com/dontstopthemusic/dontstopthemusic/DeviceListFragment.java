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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.ListFragment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;


/**
 * Visually displays some provided list of BluetoothDevice instances.
 */
public class DeviceListFragment extends ListFragment
{

	/* An instance of the list adapter */
	private DeviceListAdapter mDeviceListAdapter;

	/* The device click callback */
	private DeviceClickCallback deviceClickCallback;


	/**
	 * @param savedInstanceState If the fragment is being re-created from
	 * a previous saved state, this is the state.
	 */
	@Override
	public void onCreate ( @Nullable Bundle savedInstanceState )
	{
		/* Create the superclass */
		super.onCreate ( savedInstanceState );

		/* Create the list adapter */
		mDeviceListAdapter = new DeviceListAdapter ();
	}

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
	 * @param callback A new callback for a clicked device (may be null).
	 * @return The old callback.
	 */
	public DeviceClickCallback setDeviceClickCallback ( @Nullable DeviceClickCallback callback )
	{
		DeviceClickCallback old = deviceClickCallback;
		deviceClickCallback = callback;
		return old;
	}


	/**
	 * @param l The ListView where the click happened
	 * @param v The view that was clicked within the ListView
	 * @param position The position of the view in the list
	 * @param id The row id of the item that was clicked
	 */
	@Override
	public void onListItemClick ( @NonNull ListView l, @NonNull View v, int position, long id )
	{
		/* Call the superclass */
		super.onListItemClick ( l, v, position, id );

		/* Pass the device to the callback if there is one */
		if ( deviceClickCallback != null )
			deviceClickCallback.callback ( getDevice ( position ) );
	}



	/**
	 * @param device Add/update the entry for this device.
	 */
	public void addDevice ( BluetoothDevice device )
	{
		mDeviceListAdapter.addItem ( device );
        mDeviceListAdapter.notifyDataSetChanged ();
	}

	/**
	 * @param device Remove the entry for this device.
	 */
	public void removeDevice ( BluetoothDevice device )
	{
		mDeviceListAdapter.removeItem ( device );
		mDeviceListAdapter.notifyDataSetChanged ();
	}

    /**
     * @param devices Add/update the entry for many devices.
     */
    public void addDevices ( Iterable<BluetoothDevice> devices )
    {
        for ( BluetoothDevice device : devices )
            mDeviceListAdapter.addItem ( device );
        mDeviceListAdapter.notifyDataSetChanged ();
    }

    /**
     * Remove all devices from the list.
     */
    public void clearDevices ()
    {
        mDeviceListAdapter.clear ();
        mDeviceListAdapter.notifyDataSetChanged ();
    }



	/**
	 * @return The number of devices in the list.
	 */
	public int getDeviceCount ()
	{
		return mDeviceListAdapter.getCount ();
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

		/* A comparator for devices */
		private final DeviceComparator deviceComparator = new DeviceComparator ();

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


		/**
		 * @return An iterator over the contained items.
		 */
		@NonNull
        @Override
        public Iterator<BluetoothDevice> iterator ()
        {
            return mDevices.iterator ();
        }

        /**
		 * @param device The device to add/update in the list.
		 * @return True iff the device is newly added.
		 */
		public boolean addItem ( BluetoothDevice device )
		{
			boolean removed = mDevices.removeIf ( d -> deviceComparator.compare ( d, device ) == 0 );
			mDevices.add ( device );
			return !removed;
		}

		/**
		 * @param device The device to remove from the list.
		 * @return Whether a device was removed.
		 */
		public boolean removeItem ( BluetoothDevice device )
		{
			return mDevices.removeIf ( d -> deviceComparator.compare ( d, device ) == 0 );
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
	 * An interface used to allow custom click callbacks.
	 */
	public interface DeviceClickCallback
	{
		/**
		 * @param device The device that was just clicked.
		 */
		public void callback ( BluetoothDevice device );
	}
}
