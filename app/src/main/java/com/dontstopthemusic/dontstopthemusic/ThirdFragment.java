package com.dontstopthemusic.dontstopthemusic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    private FragmentThirdBinding binding;
    private DeviceViewModel tDevice;
    private DeviceNewDataCallback tDeviceNewDataCallback = new DeviceNewDataCallback();

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
                LineChart chart = getView().findViewById(R.id.chart);
                LineChart chart2 = getView().findViewById(R.id.chart2);

                Map<LineChart, LineData> charts = new HashMap<>();

                getActivity().runOnUiThread(() -> {
                    List<Entry> entries = new ArrayList<>();
                    List<Entry> entries2 = new ArrayList<>();
                    JSONArray array;
                    JSONArray array2;
                    JSONArray arrays;

                    try {

                        arrays = data.getJSONArray("frequency");
                        array = (JSONArray) arrays.get(0);
                        array2 = (JSONArray) arrays.get(1);

                        for (int j = 0; j < array.length(); ++j) {
                            Double val = array.getDouble(j);
                            Double val2 = array2.getDouble(j);
                            entries.add(new Entry(j, val.floatValue()));
                            entries2.add(new Entry(j, (float) val2.floatValue()));
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }


                    LineDataSet dataSet = new LineDataSet(entries, "");
                    LineDataSet dataSet2 = new LineDataSet(entries2, "");

                    dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    dataSet.setDrawValues(false);
                    dataSet.setDrawCircles(false);

                    dataSet2.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    dataSet2.setDrawValues(false);
                    dataSet2.setDrawCircles(false);

                    LineData lineData = new LineData(dataSet);
                    LineData lineData2 = new LineData(dataSet2);

                    charts.put(chart, lineData);
                    charts.put(chart2, lineData2);

                    for (LineChart c: charts.keySet()) {
                        c.setData(charts.get(c));

                        c.getXAxis().setPosition(XAxis.XAxisPosition.BOTH_SIDED);
                        c.getXAxis().setDrawGridLines(false);
                        c.getXAxis().setDrawLabels(false);

                        c.getAxisLeft().setDrawGridLines(false);
                        c.getAxisLeft().setDrawLabels(false);
                        c.getAxisLeft().setAxisMaxValue(200);
                        c.getAxisRight().setDrawGridLines(false);
                        c.getAxisRight().setDrawLabels(false);
                        c.getAxisRight().setAxisMaxValue(200);

                        c.getDescription().setEnabled(false);
                        c.getLegend().setEnabled(false);
                        c.invalidate();
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
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (tDevice.isConnected()) {
                    tDevice.registerNewDataCallback(tDeviceNewDataCallback);
                }
            }
        }).start();

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

    @Override
    public void onDestroyView() {
        tDevice.unregisterNewDataCallback(tDeviceNewDataCallback);
        super.onDestroyView();
        binding = null;
    }

}