package com.vannakittikun.alphafitness;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Rule on 11/6/2017.
 */

public class RecordWorkoutLandscape extends Fragment {
    MyDBHandler dbHandler;
    LineChart chart;
    List<Double> calories;
    List<Integer> steps;
    int time;
    int time2;
    double mph = 0;
    double maxmph = 0;
    double minmph = 0;
    float test;
    int currentSessionID;

    private DecimalFormat df;

    private TextView avg;
    private TextView max;
    private TextView min;

    LineDataSet dataSet;
    LineDataSet dataSet2;
    LineData lineData;
    List<Entry> calorieEntries;
    List<Entry> stepsEntries;

    Thread t;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("maxmph", maxmph);
        outState.putDouble("minmph", minmph);
        outState.putDouble("mph", mph);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        avg = getActivity().findViewById(R.id.avg);
        max = getActivity().findViewById(R.id.max);
        min = getActivity().findViewById(R.id.min);

        if(RecordWorkoutPortrait.workoutMode) {
            calculateMPH();
            avg.setText(df.format(mph));
            minmph = mph;
        }

        if (savedInstanceState != null) {
            maxmph = savedInstanceState.getDouble("maxmph");
            minmph = savedInstanceState.getDouble("minmph");
            mph = savedInstanceState.getDouble("mph");
            Log.d("MAXMPH", Double.toString(maxmph));

            avg.setText(df.format(mph));
            max.setText(df.format(maxmph) + " mi/hr");
            min.setText(df.format(minmph) + " mi/hr");
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_record_workout_landscape,container,false);


        df = new DecimalFormat("#.#");

        dbHandler = new MyDBHandler(getActivity(), null, null, 1);
        chart = view.findViewById(R.id.chart);
        currentSessionID = dbHandler.getCurrentSessionID();

        calories = dbHandler.getChartCalories(1);
        steps = dbHandler.getChartSteps(1);
        calorieEntries = new ArrayList<Entry>();
        stepsEntries = new ArrayList<Entry>();

        calorieEntries.add(new Entry(0, (float) 0.0));
        stepsEntries.add(new Entry(0, (float) 0.0));

        for(int i = 0; i<steps.size(); i++){
            if(i==0){
                int step = steps.get(i);
                stepsEntries.add(new Entry(time, (float) step));
            } else {
                int step = steps.get(i) - steps.get(i - 1);
                stepsEntries.add(new Entry(time, (float) step));
            }
            time+=15;
        }

        dataSet = new LineDataSet(stepsEntries, "Steps");
        dataSet.setColor(Color.BLUE);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.BLUE);

        for(int i = 0; i<calories.size(); i++){
            if(i==0){
                double calorie = calories.get(i)*10;
                calorieEntries.add(new Entry(time2, (float) calorie));
            } else {
                double calorie = (calories.get(i) - calories.get(i - 1)) * 10;
                calorieEntries.add(new Entry(time2, (float) calorie));
            }
            time2+=15;
        }

        dataSet2 = new LineDataSet(calorieEntries, "Calories Burned");
        dataSet2.setColor(Color.RED);
        dataSet2.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet2.setDrawCircles(false);
        dataSet2.setDrawValues(false);
        dataSet2.setDrawFilled(true);
        dataSet2.setFillColor(Color.RED);

        lineData = new LineData(dataSet, dataSet2);
        chart.setData(lineData);
        chart.invalidate();


        t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        if(isAdded()) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (RecordWorkoutPortrait.workoutMode) {
                                        calculateMPH();
                                        if(mph>maxmph){
                                            maxmph = mph;
                                        }
                                        if(mph<minmph){
                                            minmph = mph;
                                        }
                                        avg.setText(df.format(mph));
                                        max.setText(df.format(maxmph) + " mi/hr");
                                        min.setText(df.format(minmph) + " mi/hr");

                                        calories = dbHandler.getChartCalories(1);
                                        steps = dbHandler.getChartSteps(1);

                                        for(int i = dataSet2.getEntryCount()-1; i<calories.size(); i++){
                                            Log.d("UPDATE", Integer.toString(dataSet2.getEntryCount()) + " " + Integer.toString(calories.size()));
                                            time2+=15;
                                            double calorie = (calories.get(i) - calories.get(i - 1)) * 10;
                                            dataSet2.addEntry(new Entry(time2, (float) calorie));

                                        }
                                        for(int i = dataSet.getEntryCount()-1; i<steps.size(); i++){
                                            Log.d("UPDATE", Integer.toString(dataSet.getEntryCount()) + " " + Integer.toString(steps.size()));
                                            time+=15;
                                            double step = (steps.get(i) - steps.get(i - 1));
                                            dataSet.addEntry(new Entry(time, (float) step));
                                        }


                                        dataSet.notifyDataSetChanged();
                                        dataSet2.notifyDataSetChanged();
                                        lineData.notifyDataChanged();
                                        chart.notifyDataSetChanged();
                                        chart.invalidate();
                                    }
                                }
                            });
                        }
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();

        return view;
    }

    public void calculateMPH(){
        double distance = dbHandler.getCurrentSessionDistance(currentSessionID);
        long currentTime = dbHandler.getCurrentSessionTime(currentSessionID);
        mph = ((distance* 0.000621371)*3600000)/currentTime;
    }
}
