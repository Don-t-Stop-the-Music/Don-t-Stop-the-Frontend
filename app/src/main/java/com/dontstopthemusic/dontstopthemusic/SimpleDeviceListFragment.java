package com.dontstopthemusic.dontstopthemusic;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;



/**
 * An extension of BaseDeviceListFragment, specifying how views should be populated.
 */
public class SimpleDeviceListFragment extends BaseDeviceListFragment
{

	/**
	 * @return The identifier for the layout to use for views.
	 */
	@Override
	protected int getListItemLayout ()
	{
		return R.layout.listitem_device;
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

		final String name = device.getInfo ().getName ();
		if ( name != null && name.length () > 0 )
			deviceName.setText ( name );
		else
			deviceName.setText ( R.string.unknown_device );
		deviceAddress.setText ( device.getInfo ().getAddress () );
	}
}
