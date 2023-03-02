package com.dontstopthemusic.dontstopthemusic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.IBinder;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.dontstopthemusic.dontstopthemusic.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private DeviceViewModel mDevice;

    private BluetoothService mBluetoothService;
    private BluetoothServiceConnection mBluetoothServiceConnection= new BluetoothServiceConnection();

    private DeviceStatusChangeCallback mDeviceStatusChangeCallback=new DeviceStatusChangeCallback();

    private DeviceNewDataCallback mDeviceNewDataCallback=new DeviceNewDataCallback();

    private static JSONObject MostUpdatedJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDevice = new ViewModelProvider(this).get(DeviceViewModel.class);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        /* Bind to the Bluetooth service */
        bindService (
                new Intent( this, BluetoothService.class ),
                mBluetoothServiceConnection,
                Context.BIND_AUTO_CREATE );


    }

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

            mDevice.assignDevice(mBluetoothService);

            if (mDevice.isNull()) {
                finish();
            }
            else {
                mDevice.registerNewDataCallback(mDeviceNewDataCallback);
                mDevice.registerStatusChangeCallback(mDeviceStatusChangeCallback);
            }
        }
    }

    private class DeviceStatusChangeCallback implements Device.StatusChangeCallback
    {
        @Override
        public void onStatusChange ( Device device )
        {
            if (!device.isConnected())
                finish();
        }
    }

    private class DeviceNewDataCallback implements Device.NewDataCallback
    {
        @Override
        public void onNewData (Device device , JSONObject jsonobject)
        {
            //callback data
            MostUpdatedJson=jsonobject;

        }
    }

    public static JSONObject getUpdatedJson(){
        return MostUpdatedJson;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()){
            case (R.id.action_settings): {
                return true;
            }
            case (R.id.HelpFragment): {
                NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
                return NavigationUI.onNavDestinationSelected(item,navController)
                        || super.onOptionsItemSelected(item);
            }
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDevice.isNull()){
            mDevice.unregisterStatusChangeCallback(mDeviceStatusChangeCallback);
            mDevice.unregisterNewDataCallback(mDeviceNewDataCallback);
        }
    }
}