package com.dontstopthemusic.dontstopthemusic;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * An extension of BaseDeviceListFragment, specifying how views should be populated.
 */
public class StatusDeviceListFragment extends BaseDeviceListFragment
{

	/**
	 * @return The identifier for the layout to use for views.
	 */
	@Override
	protected int getListItemLayout ()
	{
		return R.layout.listitem_status_device;
	}

	/**
	 * @param view The view to update.
	 * @param device The device to update the view to.
	 */
	@Override
	protected void updateListItemView ( View view, Device device ) throws SecurityException
	{
		/* Get views */
		TextView deviceName = view.findViewById ( R.id.device_name );
		TextView deviceAddress = view.findViewById ( R.id.device_address );
		TextView deviceStatus = view.findViewById ( R.id.device_status );
		ImageView deviceArrow = view.findViewById ( R.id.device_arrow );
		TextView disconnectButton = view.findViewById ( R.id.disconnect_device );

		/* Set the device name */
		final String name = device.getInfo ().getName ();
		if ( name != null && name.length () > 0 )
			deviceName.setText ( name );
		else
			deviceName.setText ( R.string.unknown_device );

		deviceAddress.setText ( device.getInfo ().getAddress () );
		/* Set the address */

		/* Set connection information */
		if ( device.isConnected () )
		{
			deviceArrow.setVisibility ( View.VISIBLE );
			disconnectButton.setVisibility ( View.VISIBLE );
			deviceStatus.setText ( getString ( R.string.bluetooth_connected ) );
		}
		else if ( device.isConnecting () )
		{
			deviceArrow.setVisibility ( View.INVISIBLE );
			disconnectButton.setVisibility ( View.INVISIBLE );
			deviceStatus.setText ( getString ( R.string.blutooth_connecting ) );
		}
		else
		{
			deviceArrow.setVisibility ( View.INVISIBLE );
			disconnectButton.setVisibility ( View.INVISIBLE );
			deviceStatus.setText ( getString ( R.string.bluetooth_disconnected ) );
		}

		/* Set the onclick listener for disconnecting the device */
		disconnectButton.setOnClickListener ( View -> device.close () );
	}
}
