package com.vannakittikun.alphafitness;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by Rule on 11/13/2017.
 */

public class UserProfileActivity extends AppCompatActivity{

    private MyDBHandler dbHandler;
    private User user;

    private TextView nameText;
    private TextView weightText;
    private TextView genderText;
    private EditText nameEdit;
    private Spinner genderSpinner;
    private EditText weightEdit;

    private TextView weeklyDistanceText;
    private TextView weeklyWorkoutsText;
    private TextView weeklyTimeText;

    private boolean editMode;
    private DecimalFormat df;
    Thread t;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        getSupportActionBar().setHomeButtonEnabled(true);
        df = new DecimalFormat("#.###");

        dbHandler = new MyDBHandler(this, null, null, 1);
        genderSpinner = findViewById(R.id.genderSpinner);

        user = dbHandler.getUser(1);

        //RecordWorkoutPortrait.weeklyDistance = dbHandler.getWeeklyDistance(user.getId());
        //RecordWorkoutPortrait.weeklyWorkouts = dbHandler.getWeeklyWorkouts(user.getId());
        //RecordWorkoutPortrait.weeklyTime = dbHandler.getWeeklyTime(user.getId());

        weeklyDistanceText = findViewById(R.id.weeklyDistanceText);
        weeklyDistanceText.setText(df.format(metersToMiles(RecordWorkoutPortrait.weeklyDistance)) + " mi.");
        weeklyWorkoutsText = findViewById(R.id.weeklyWorkoutsText);
        weeklyWorkoutsText.setText(RecordWorkoutPortrait.weeklyWorkouts + " time(s)");
        weeklyTimeText = findViewById(R.id.weeklyTimeText);
        weeklyTimeText.setText(getDurationBreakdown(RecordWorkoutPortrait.weeklyTime));

        t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                weeklyDistanceText.setText(df.format(metersToMiles(RecordWorkoutPortrait.weeklyDistance)) + " mi.");
                                weeklyWorkoutsText.setText(RecordWorkoutPortrait.weeklyWorkouts + " time(s)");
                                if(RecordWorkoutPortrait.workoutMode) {
                                    RecordWorkoutPortrait.weeklyTime += 1000;
                                }
                                weeklyTimeText.setText(getDurationBreakdown(RecordWorkoutPortrait.weeklyTime));
                                Log.d("UpdateDetails", "Updating textviews");
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();

        nameText = findViewById(R.id.nameText);
        weightText = findViewById(R.id.weightText);
        genderText = findViewById(R.id.genderText);

        nameEdit = findViewById(R.id.nameEdit);
        weightEdit = findViewById(R.id.weightEdit);
        genderSpinner = findViewById(R.id.genderSpinner);

        nameText.setText(user.getName());
        weightText.setText(Integer.toString(user.getWeight()));
        genderText.setText(user.getGender());
        Log.d("USER", "Name: " + user.getName() + " Gender: " + user.getGender() + " Weight: " + user.getWeight());

        nameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(editMode) {
                    dbHandler.updateUser(1, String.valueOf(nameEdit.getText()), genderSpinner.getSelectedItem().toString(), Integer.parseInt("0" + weightEdit.getText().toString()));
                }
                //Log.d("USER", user.getName());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        weightEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(editMode) {
                    dbHandler.updateUser(1, String.valueOf(nameEdit.getText()), genderSpinner.getSelectedItem().toString(), Integer.parseInt("0" + weightEdit.getText().toString()));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(editMode) {
                    dbHandler.updateUser(1, String.valueOf(nameEdit.getText()), genderSpinner.getSelectedItem().toString(), Integer.parseInt("0" + weightEdit.getText().toString()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        editMode = false;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        t.interrupt();
    }

    /**
     * Convert a millisecond duration to a string format
     *
     * @param millis A duration to convert to a string form
     * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
     */
    public static String getDurationBreakdown(long millis)
    {
        if(millis < 0)
        {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        long milliseconds = millis % 1000;

        return String.format("%d Days %d Hrs %d Mins %d Secs", days, hours, minutes, seconds);
    }

    public double metersToMiles(double meters) {
        return meters * 0.000621371;
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user_profile, menu); //inflate our menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                this.finish();
                return true;

            case R.id.item_edit:
                editUser();
                if(editMode){
                    item.setTitle("Save");
                } else {
                    item.setTitle("Edit");
                    hideKeyboard(this);
                }
                return true;

            case R.id.item_reset:

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void editUser(){
        if(!editMode){
            editMode = true;
            user = dbHandler.getUser(1);
            Log.d("USER", "Name: " + user.getName() + " Gender: " + user.getGender() + " Weight: " + user.getWeight());

            nameText.setVisibility(View.INVISIBLE);
            genderText.setVisibility(View.INVISIBLE);
            weightText.setVisibility(View.INVISIBLE);

            nameEdit.setText(user.getName());
            genderSpinner.setSelection(getIndex(genderSpinner, user.getGender()));
            weightEdit.setText(Integer.toString(user.getWeight()));

            nameEdit.setVisibility(View.VISIBLE);
            genderSpinner.setVisibility(View.VISIBLE);
            weightEdit.setVisibility(View.VISIBLE);


        } else {
            editMode = false;
            user = dbHandler.getUser(1);
            Log.d("USER", "Name: " + user.getName() + " Gender: " + user.getGender() + " Weight: " + user.getWeight());

            nameText.setVisibility(View.VISIBLE);
            genderText.setVisibility(View.VISIBLE);
            weightText.setVisibility(View.VISIBLE);

            nameText.setText(user.getName());
            weightText.setText(Integer.toString(user.getWeight()));
            genderText.setText(user.getGender());

            nameEdit.setVisibility(View.INVISIBLE);
            genderSpinner.setVisibility(View.INVISIBLE);
            weightEdit.setVisibility(View.INVISIBLE);
        }
    }

    private int getIndex(Spinner spinner, String myString)
    {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                index = i;
                break;
            }
        }
        return index;
    }
}
