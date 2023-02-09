package com.dontstopthemusic.dontstopthemusic;

import android.os.Bundle;

import com.dontstopthemusic.dontstopthemusic.databinding.ActivityBluetoothBinding;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.dontstopthemusic.dontstopthemusic.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

public class BluetoothConnectActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityBluetoothBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBluetoothBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_bluetooth);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);


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
        /*switch(item.getItemId()){
            case (R.id.action_settings): {
                return true;
            }
            case (R.id.HelpFragment): {
                NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_bluetooth);
                return NavigationUI.onNavDestinationSelected(item,navController)
                        || super.onOptionsItemSelected(item);
            }
            default:
                return super.onOptionsItemSelected(item);
        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_bluetooth);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}