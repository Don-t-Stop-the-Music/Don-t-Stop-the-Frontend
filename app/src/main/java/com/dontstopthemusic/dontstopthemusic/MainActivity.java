package com.dontstopthemusic.dontstopthemusic;

import static java.util.Objects.isNull;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.dontstopthemusic.dontstopthemusic.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private ConnectThread connectThread = null;
    private BluetoothDevice raspberrypi;
    private String TAG = "Main Activity: ";
    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals( intent.getAction() )) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String deviceName = "";

                try{deviceName = device.getName();}
                catch(SecurityException e){Log.e(TAG, "device.getName() failed (lacking security privilege)", e);}

                if(Objects.equals(deviceName, "raspberrypi2")){
                    Log.e(TAG, "found a device!: " + deviceName);
                    new ConnectThread(device).start();
                }//should check MAC address instead to be more robust
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action (Hello World)", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                Log.e(TAG, "connection found?: " + !isNull(connectThread));
                if(!isNull(connectThread)){connectThread.start();}
            }
        });

        //---

        binding.fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action (Hello World)", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                boolean b = false;
                Log.e(TAG, "starting discovery");
                try{b = bluetoothAdapter.startDiscovery();}catch(SecurityException e) {
                    Log.e(TAG, "bluetoothAdapter.startDiscovery() failed (lacking security privilege)", e);
                }
                if(!b){Log.e(TAG, "bluetoothAdapter.startDiscovery() failed (unknown)");}
            }
        });

        //---

        binding.editTextHelloWorld.setText("Moo!");

        context = getApplicationContext();

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        //registerReceiver(receiver, null); //immediately crashes lol

        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();


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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
        if(!isNull(connectThread)){connectThread.cancel();}
        super.onDestroy();

    }
}