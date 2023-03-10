package com.dontstopthemusic.dontstopthemusic;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.dontstopthemusic.dontstopthemusic.databinding.FragmentThirdBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThirdFragment extends Fragment {
    //home page
    private FragmentThirdBinding binding;

    //for bluetooth connection
    private DeviceViewModel tDevice;
    private DeviceNewDataCallback tDeviceNewDataCallback = new DeviceNewDataCallback();

    //aesthetics
    private String buttonColor = "#0934B6";

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentThirdBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private class DeviceNewDataCallback implements Device.NewDataCallback {
        @Override
        public void onNewData(Device device, @Nullable JSONObject data) {
            try {
                LineChart stereoChart = getView().findViewById(R.id.chart_stereo);
                LineChart monitorChart = getView().findViewById(R.id.chart_monitor);
                // get charts from xml

                Map<LineChart, LineData> charts = new HashMap<>();

                getActivity().runOnUiThread(() -> {
                    List<Entry> stereoEntries = new ArrayList<>();
                    List<Entry> monitorEntries = new ArrayList<>();

                    JSONArray stereoArray;
                    JSONArray monitorArray;
                    JSONArray eqArrays;
                    JSONArray hissArray;
                    JSONArray feedbackArray;

                    try {

                        eqArrays = data.getJSONArray("frequency");
                        hissArray = data.getJSONArray("hiss");
                        feedbackArray = data.getJSONArray("feedback");

                        if (hissArray.getBoolean(0) || hissArray.getBoolean(1)) {
                            binding.buttonDebugHiss.setBackgroundColor(Color.RED);
                        } else {
                            binding.buttonDebugHiss.setBackgroundColor(Color.parseColor(buttonColor));
                        } // hiss button turns red if at least one hiss value is true

                        if (feedbackArray.getJSONArray(0).length() > 0 ||
                                feedbackArray.getJSONArray(1).length() > 0) {
                            binding.buttonDebugFeedback.setBackgroundColor(Color.RED);
                        } else {
                            binding.buttonDebugFeedback.setBackgroundColor(Color.parseColor(buttonColor));
                        } // feedback button turns red if feedback array is empty

                        stereoArray = (JSONArray) eqArrays.get(0);
                        monitorArray = (JSONArray) eqArrays.get(1);

                        boolean silent = true;

                        for (int i = 0; i < stereoArray.length(); ++i) {
                            Double u = stereoArray.getDouble(i);
                            stereoEntries.add(new Entry(i, u.floatValue()));

                            if (u >= 0.1) {
                                silent = false; // check for at least one frequency entry
                            }
                        }

                        for (int j = 0; j < monitorArray.length(); ++j) {
                            Double v = monitorArray.getDouble(j);
                            monitorEntries.add(new Entry(j, v.floatValue()));
                        } // chart entries

                        if (silent) {
                            binding.buttonDebugSilence.setBackgroundColor(Color.RED);
                        } else {
                            binding.buttonDebugSilence.setBackgroundColor(Color.parseColor(buttonColor));
                        } // silence button turns red if frequency array is empty


                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    LineDataSet stereoDataSet = new LineDataSet(stereoEntries, "");
                    LineDataSet monitorDataSet = new LineDataSet(monitorEntries, "");

                    stereoDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    stereoDataSet.setDrawValues(false);
                    stereoDataSet.setDrawCircles(false);

                    monitorDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    monitorDataSet.setDrawValues(false);
                    monitorDataSet.setDrawCircles(false);

                    LineData stereoLineData = new LineData(stereoDataSet);
                    LineData monitorLineData = new LineData(monitorDataSet);

                    charts.put(stereoChart, stereoLineData);
                    charts.put(monitorChart, monitorLineData);

                    for (LineChart c: charts.keySet()) {
                        c.setData(charts.get(c));
                        c.setDrawBorders(false);

                        c.getXAxis().setPosition(XAxis.XAxisPosition.BOTH_SIDED);
                        c.getXAxis().setDrawGridLines(false);
                        c.getXAxis().setDrawLabels(false);
                        c.getXAxis().setDrawAxisLine(false);

                        c.getAxisLeft().setDrawGridLines(false);
                        c.getAxisLeft().setDrawLabels(false);
                        c.getAxisLeft().setDrawAxisLine(false);
                        c.getAxisRight().setDrawGridLines(false);
                        c.getAxisRight().setDrawLabels(false);
                        c.getAxisRight().setDrawAxisLine(false); // remove borders and grid lines

                        c.getAxisLeft ().setAxisMaxValue ( 6 );
                        c.getAxisRight ().setAxisMaxValue ( 6 );
                        // stops the maximum y-axis value from being calculated automatically

                        c.getDescription().setEnabled(false);
                        c.getLegend().setEnabled(false);
                        c.invalidate(); // redraw charts
                    }
                });
            } catch (NullPointerException ignore) {}
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tDevice = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while ( true )
                {
                    if ( tDevice.isConnected () )
                    {
                        tDevice.registerNewDataCallback ( tDeviceNewDataCallback );
                        return;
                    }
                    Thread.yield ();
                } // register callback when device is connected
            }
        }).start();

        //navigation from homepage to the three debug flows
        binding.buttonDebugFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(ThirdFragment.this)
                        .navigate(R.id.action_ThirdFragment_to_FirstFragment);
            }
        });

        binding.buttonDebugSilence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(ThirdFragment.this)
                        .navigate(R.id.action_ThirdFragment_to_SecondFragment);
            }
        });

        binding.buttonDebugHiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(ThirdFragment.this)
                        .navigate(R.id.action_ThirdFragment_to_FourthFragment);
            }
        });
    }

    //android back button returns to bluetooth connection page instead of the (default behaviour) previous fragment
    OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
        @Override
        public void handleOnBackPressed() {
            // Handle the back button event
             requireActivity().finish();
             //Returns to previous activity i.e. bluetooth connection
        }
    };
    @Override
    public void onDestroyView() {
        tDevice.unregisterNewDataCallback(tDeviceNewDataCallback);
        super.onDestroyView();
    }
}