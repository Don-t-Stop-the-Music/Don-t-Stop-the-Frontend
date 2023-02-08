package com.dontstopthemusic.dontstopthemusic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.stream.IntStream;


/**
 * Visually displays some provided list of Device instances.
 */
public class DeviceListFragment extends ListFragment
{

	/* An instance of the list adapter */
	private DeviceListAdapter mDeviceListAdapter;

	/* The device click callback */
	private DeviceClickCallback mDeviceClickCallback;

	/* Whether or not to show the connection status of devices */
	private boolean mShowConnectionStatus = false;

	/* The callback passed to devices to allow them to refresh the list */
	private final Device.DeviceStatusChangeCallback mDeviceStatusChangeCallback =
			Device -> refreshDevices ();



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
		DeviceClickCallback old = mDeviceClickCallback;
		mDeviceClickCallback = callback;
		return old;
	}


	/**
	 * @param show Whether the list should show the connection status of its elements
	 */
	public void setShowConnectionStatus ( boolean show )
	{
		boolean change = mShowConnectionStatus != show;
		mShowConnectionStatus = show;
		mDeviceListAdapter.notifyDataSetChanged ();
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
		if ( mDeviceClickCallback != null )
			mDeviceClickCallback.callback ( getDevice ( position ) );
	}



	/**
	 * @param device Add/update the entry for this device.
	 * @return The index of the added device.
	 */
	public int addDevice ( Device device )
	{
		int index = mDeviceListAdapter.addItem ( device );
		mDeviceListAdapter.notifyDataSetChanged ();
		return index;
	}

	/**
	 * @param device Remove the entry for this device.
	 * @return Whether the device was present in the list.
	 */
	public boolean removeDevice ( Device device )
	{
		boolean removed = mDeviceListAdapter.removeItem ( device );
		mDeviceListAdapter.notifyDataSetChanged ();
		return removed;
	}

    /**
     * @param devices Add/update the entry for many devices.
	 * @return The indices of the added devices.
     */
    public int[] addDevices ( Iterable<Device> devices )
    {
		/* Add all of the indices to a list */
		ArrayList<Integer> indices = new ArrayList<> ();
        for ( Device device : devices )
            indices.add ( mDeviceListAdapter.addItem ( device ) );
        mDeviceListAdapter.notifyDataSetChanged ();
		return indices.stream().mapToInt ( Integer::intValue ).toArray ();
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
	 * Force the list to update.
	 */
	public void refreshDevices ()
	{
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
    public Device getDevice ( int i )
    {
        return ( Device ) mDeviceListAdapter.getItem ( i );
    }



	/**
	 * The list adapter for the fragment
	 */
	private class DeviceListAdapter extends BaseAdapter implements Iterable<Device>
	{

		/* A list of bluetooth devices in the adapter */
		private final ArrayList<Device> mDevices;

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
        public Iterator<Device> iterator ()
        {
            return mDevices.iterator ();
        }

        /**
		 * @param device The device to add/update in the list.
		 * @return The index of the added device.
		 */
		public int addItem ( Device device )
		{
			/* Try to find a matching device */
			int index = IntStream.range ( 0, mDevices.size () )
					.filter ( i -> deviceComparator.compare ( mDevices.get ( i ), device ) == 0 )
					.findFirst ()
					.orElse ( mDevices.size () );

			/* Either add a new device, or modify the existing one */
			if ( index == mDevices.size () )
				mDevices.add ( device );
			else
			{
				mDevices.get ( index ).unregisterStatusChangeCallback ( mDeviceStatusChangeCallback );
				mDevices.set ( index, device );
			}

			/* Register the status change callback */
			device.registerStatusChangeCallback ( mDeviceStatusChangeCallback );

			/* Return the index of the device */
			return index;
		}

		/**
		 * @param device The device to remove from the list.
		 * @return Whether a device was removed.
		 */
		public boolean removeItem ( Device device )
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
		public void updateText ( Device device ) throws SecurityException
		{
			final String name = device.getInfo ().getName ();
			if ( name != null && name.length () > 0 )
				this.deviceName.setText ( name );
			else
				this.deviceName.setText ( R.string.unknown_device );
			this.deviceAddress.setText ( device.getInfo ().getAddress () );
		}
	}


	/**
	 * A comparator over MAC addresses of devices.
	 */
	static private class DeviceComparator implements Comparator<Device>
	{
		/**
		 * @param d1 The first device
		 * @param d2 The second device
		 * @return The comparison of the MAC addresses of devices.
		 */
		@Override
		public int compare ( Device d1, Device d2 )
		{
			long cmp = addressToInt ( d1.getInfo ().getAddress () ) - addressToInt ( d2.getInfo ().getAddress () );
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
		public void callback ( Device device );
	}
}
