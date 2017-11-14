package com.vannakittikun.alphafitness;

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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;

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

    private boolean editMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        getSupportActionBar().setHomeButtonEnabled(true);

        dbHandler = new MyDBHandler(this, null, null, 1);
        genderSpinner = findViewById(R.id.genderSpinner);

        user = dbHandler.getUser(1);
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
