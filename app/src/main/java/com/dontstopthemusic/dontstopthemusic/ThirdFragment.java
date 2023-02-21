package com.dontstopthemusic.dontstopthemusic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.dontstopthemusic.dontstopthemusic.databinding.FragmentThirdBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ThirdFragment extends Fragment {

    private FragmentThirdBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentThirdBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        LineChart chart = (LineChart) getView().findViewById(R.id.chart);
        LineChart chart2 = (LineChart) getView().findViewById(R.id.chart2);
        List<LineChart> charts = new ArrayList<>();
        charts.add(chart);
        charts.add(chart2);

        List<Entry> entries = new ArrayList<>();

        for (int j = 0; j < 160; j++) {
            entries.add(new Entry(j, new Random().nextFloat() * 150));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Label");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        LineData lineData = new LineData(dataSet);

        for (LineChart c: charts) {
            c.setData(lineData);
            c.getXAxis().setPosition(XAxis.XAxisPosition.BOTH_SIDED);
            c.getXAxis().setDrawGridLines(false);
            c.getAxisLeft().setDrawGridLines(false);
            c.getAxisRight().setDrawGridLines(false);
            c.getXAxis().setDrawLabels(false);
            c.getAxisLeft().setDrawLabels(false);
            c.getAxisRight().setDrawLabels(false);
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