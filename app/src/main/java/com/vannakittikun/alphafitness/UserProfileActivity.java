package com.vannakittikun.alphafitness;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
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
    private TextView weeklyStepsText;
    private TextView weeklyCaloriesBurnedText;

    private TextView allTimeDistanceText;
    private TextView allTimeWorkoutsText;
    private TextView allTimeTimeText;
    private TextView allTimeStepsText;
    private TextView allTimeCaloriesBurnedText;

    private long allTimeTime = 0;
    private long weeklyTimeTime = 0;

    private boolean editMode;
    private DecimalFormat df;
    private DecimalFormat df2;
    Thread t;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        getSupportActionBar().setHomeButtonEnabled(true);
        df = new DecimalFormat("#.###");
        df2 = new DecimalFormat("#.##");

        dbHandler = new MyDBHandler(this, null, null, 1);
        genderSpinner = findViewById(R.id.genderSpinner);

        user = dbHandler.getUser(1);

        //RecordWorkoutPortrait.weeklyDistance = dbHandler.getWeeklyDistance(user.getId());
        //RecordWorkoutPortrait.weeklyWorkouts = dbHandler.getWeeklyWorkouts(user.getId());
        //RecordWorkoutPortrait.weeklyTime = dbHandler.getWeeklyTime(user.getId());

        weeklyDistanceText = findViewById(R.id.weeklyDistanceText);
        weeklyWorkoutsText = findViewById(R.id.weeklyWorkoutsText);
        weeklyTimeText = findViewById(R.id.weeklyTimeText);
        weeklyStepsText = findViewById(R.id.weeklyStepsText);
        weeklyCaloriesBurnedText = findViewById(R.id.weeklyCaloriesBurnedText);

        weeklyTimeTime = dbHandler.getWeeklyTime(1);

        weeklyDistanceText.setText(df.format(metersToMiles(dbHandler.getWeeklyDistance(1))) + " mi.");
        weeklyWorkoutsText.setText(dbHandler.getWeeklyWorkouts(1) + " time(s)");
        weeklyTimeText.setText(getDurationBreakdown(weeklyTimeTime));
        weeklyStepsText.setText(dbHandler.getWeeklySteps(1) + " step(s)");
        weeklyCaloriesBurnedText.setText(df2.format(dbHandler.getWeeklyCaloriesBurned(1)));

        allTimeDistanceText = findViewById(R.id.allTimeDistanceText);
        allTimeWorkoutsText = findViewById(R.id.allTimeWorkoutsText);
        allTimeTimeText = findViewById(R.id.allTimeTimeText);
        allTimeStepsText = findViewById(R.id.allTimeStepsText);
        allTimeCaloriesBurnedText = findViewById(R.id.allTimeCaloriesBurnedText);


        allTimeTime = dbHandler.getAllTimeTime(1);

        allTimeDistanceText.setText(df.format(metersToMiles(dbHandler.getAllTimeDistance(1))) + " mi.");
        allTimeWorkoutsText.setText(dbHandler.getAllTimeWorkouts(1) + " time(s)");
        allTimeTimeText.setText(getDurationBreakdown(allTimeTime));
        allTimeStepsText.setText(dbHandler.getAllTimeSteps(1) + " step(s)");
        allTimeCaloriesBurnedText.setText(df2.format(dbHandler.getAllTimeCaloriesBurned(1)));

        t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(RecordWorkoutPortrait.workoutMode) {
                                    //weeklyTimeTime += 1000;

                                    weeklyDistanceText.setText(df.format(metersToMiles(dbHandler.getWeeklyDistance(1))) + " mi.");
                                    weeklyWorkoutsText.setText(dbHandler.getWeeklyWorkouts(1) + " time(s)");
                                    weeklyTimeText.setText(getDurationBreakdown(dbHandler.getWeeklyTime(1)));
                                    weeklyStepsText.setText(dbHandler.getWeeklySteps(1) + " step(s)");

                                    allTimeTime += 1000;

                                    allTimeDistanceText.setText(df.format(metersToMiles(dbHandler.getAllTimeDistance(1))) + " mi.");
                                    allTimeWorkoutsText.setText(dbHandler.getAllTimeWorkouts(1) + " time(s)");
                                    allTimeTimeText.setText(getDurationBreakdown(allTimeTime));
                                    allTimeStepsText.setText(dbHandler.getAllTimeSteps(1) + " step(s)");
                                    allTimeCaloriesBurnedText.setText(df2.format(dbHandler.getAllTimeCaloriesBurned(1)));
                                }
                                //Log.d("UpdateDetails", "Updating textviews");
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
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                dbHandler.resetDetails(1);
                                Intent intent = getIntent();
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                finish();
                                startActivity(intent);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Reset all records");
                builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
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
