package com.vannakittikun.alphafitness;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class RecordWorkoutActivity extends AppCompatActivity implements RecordWorkoutPortrait.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_workout);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
