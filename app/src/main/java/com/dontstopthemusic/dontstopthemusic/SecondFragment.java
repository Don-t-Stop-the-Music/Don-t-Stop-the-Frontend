package com.dontstopthemusic.dontstopthemusic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.dontstopthemusic.dontstopthemusic.databinding.FragmentSecondBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });

        LineChart chart = (LineChart) getView().findViewById(R.id.chart);
        LineChart chart2 = (LineChart) getView().findViewById(R.id.chart2);
        List<LineChart> charts = new ArrayList<>();
        charts.add(chart);
        charts.add(chart2);

        int[] dataObjects = {5, 8, 2, 7, 3, 12, 1};
        List<Entry> entries = new ArrayList<>();
        int i = 0;

        for (int data : dataObjects) {
            entries.add(new Entry(i, data));
            i += 1;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Label");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        LineData lineData = new LineData(dataSet);

        for (LineChart c: charts) {
            c.setData(lineData);
            c.getXAxis().setPosition(XAxis.XAxisPosition.BOTH_SIDED);
            c.getXAxis().setDrawGridLines(false);
            c.getAxisLeft().setDrawGridLines(false);
            c.getAxisRight().setDrawGridLines(false);
            c.getDescription().setEnabled(false);
            c.getLegend().setEnabled(false);
            c.invalidate();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}