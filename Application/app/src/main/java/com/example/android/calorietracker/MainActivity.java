package com.example.android.calorietracker;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements OnItemSelectedListener {

    private HashMap<String, Double> activity_map = new HashMap<String, Double>();
    String [] activity = {"pushup", "situp", "squats", "leg-lift", "plank",
                          "jumping jacks", "pullup", "cycling", "walking",
                          "jogging", "swimming", "stair-climbing"};
    double [] reps_needed = {3.5, 2, 2.25, .25, .25, .10, 1, .12, .20,
                            .12, .13, .15};
    String [] rep_ex = {"pushup", "situp", "squats", "pullup"};
    ArrayList <String> rep_arr = new ArrayList<String>();
    ArrayList<String> equiv_ex = new ArrayList<String>();
    RadioButton repetitions, minutes;
    String sport;
    String type, text1, text2,text3,text4;
    double duration, calories_burned;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner spinner = (Spinner) findViewById(R.id.activities_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.activities_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        populateMap();

        if (savedInstanceState != null) {
//            Log.d("DEBUG", "HELLO ITS ME!!!!");
            text1 = savedInstanceState.getString("txt1");
            text2 = savedInstanceState.getString("txt2");
            text3 = savedInstanceState.getString("txt3");
            text4 = savedInstanceState.getString("txt4");
            sport = savedInstanceState.getString("sprt");
            calories_burned = savedInstanceState.getDouble("cal_burned");
            int dsply_state = savedInstanceState.getInt("display_state");
            int dsply_state2 = savedInstanceState.getInt("display2_state");
            LinearLayout display = (LinearLayout) findViewById(R.id.display_calorie);

            if (dsply_state == View.VISIBLE) {
//                Log.d("IN HERE!", "DEBUGGG");
                TextView calorie = (TextView) findViewById(R.id.calories);
                calorie.setTypeface(Typeface.DEFAULT_BOLD);
                calorie.setText( "Burned " + Double.toString(calories_burned) + " calories!");
                display.setVisibility(View.VISIBLE);
            }
            LinearLayout display2 = (LinearLayout) findViewById(R.id.equivalent_exercises);
            if (dsply_state2 == View.VISIBLE) {
//                Log.d("IN HERE!", "DEBUGGG2");
                if (sport != null) {
                    outputText(sport);
                    display2.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        LinearLayout display = (LinearLayout) findViewById(R.id.display_calorie);
        LinearLayout display2 = (LinearLayout) findViewById(R.id.equivalent_exercises);
        savedInstanceState.putString("txt1", text1);
        savedInstanceState.putString("txt2", text2);
        savedInstanceState.putString("txt3", text3);
        savedInstanceState.putString("txt4", text4);
        savedInstanceState.putString("sprt", sport);
        savedInstanceState.putInt("display_state", display.getVisibility());
        savedInstanceState.putInt("display_state2", display2.getVisibility());
        savedInstanceState.putDouble("cal_burned", calories_burned);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    /* Populates the hashmap where the key is the activity and value
       is the rep/calorie.
    */
    private void populateMap() {
        for (int i = 0; i < activity.length; i += 1) {
            activity_map.put(activity[i], reps_needed[i]);
        }

        for (int j = 0; j < rep_ex.length; j += 1) {
            rep_arr.add(rep_ex[j]);
        }
    }


    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {

        String selected_act = parent.getItemAtPosition(pos).toString().toLowerCase();
        if (sport != null && !sport.equals(selected_act)) {
            LinearLayout display = (LinearLayout) findViewById(R.id.display_calorie);
            LinearLayout display2 = (LinearLayout) findViewById(R.id.equivalent_exercises);
            display.setVisibility(View.INVISIBLE);
            display2.setVisibility(View.INVISIBLE);
        }
        TextView type = (TextView) findViewById(R.id.type);
        if (!rep_arr.contains(selected_act)) {
            type.setText(" Minutes");
        } else {
            type.setText(" Repetitions");
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void outputCaloriesBurned(View v){

        closeKeypad();

        Spinner mySpinner = (Spinner) findViewById(R.id.activities_spinner);
        sport = mySpinner.getSelectedItem().toString().toLowerCase();
        EditText dur = (EditText) findViewById(R.id.duration);
        TextView calorie = (TextView) findViewById(R.id.calories);
        if (dur.getText().toString().length() == 0) {
            Toast.makeText(this, (String) "Duration number field is empty",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        duration = Double.parseDouble(dur.getText().toString());
        calories_burned = caloriesSpent(duration, sport, "", true);
        calorie.setTypeface(Typeface.DEFAULT_BOLD);
        calorie.setText( "Burned " + Double.toString(calories_burned) + " calories!");

        LinearLayout display = (LinearLayout) findViewById(R.id.display_calorie);
        display.setVisibility(View.VISIBLE);
        calculateEquivalentExercises(duration, sport);
        outputText(sport);
        LinearLayout display2 = (LinearLayout) findViewById(R.id.equivalent_exercises);
        display2.setVisibility(View.VISIBLE);
    }

    /* hides the editTextBox when button is clicked. */
    private void closeKeypad(){
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private double caloriesSpent(double duration, String sport, String cmp_sport, boolean equiv) {
        double spt = activity_map.get(sport);

        if (equiv) {
            return  Math.round(duration / spt * 100.0) / 100.0;
        }
        double cmp_spt = activity_map.get(cmp_sport);
        return Math.round(((duration * spt) / cmp_spt) * 100.0) / 100.0;
    }

    private String [] calculateEquivalentExercises(double duration, String sport) {
        TextView pushup = (TextView) findViewById(R.id.txt1);
        double amt = caloriesSpent(duration, "pushup", sport, false);
        text1 = amt + " reps of pushups";

        TextView situp = (TextView) findViewById(R.id.txt2);
        double amt2 = caloriesSpent(duration, "situp", sport, false);
        text2 = amt2 + " reps of situps";

        TextView jumping_jacks = (TextView) findViewById(R.id.txt3);
        double amt3 = caloriesSpent(duration, "jumping jacks", sport, false);
        text3 = amt3 + " minutes of jumping jacks";

        TextView jogging = (TextView) findViewById(R.id.txt4);
        double amt4 = caloriesSpent(duration, "jogging", sport, false);
        text4 = amt4 + " minutes of jogging ";

        String[] arr = {text1, text2, text3, text4};
        return arr;
    }

    private void outputText(String sport) {
        TextView pushup = (TextView) findViewById(R.id.txt1);
        TextView situp = (TextView) findViewById(R.id.txt2);
        TextView jumping_jacks = (TextView) findViewById(R.id.txt3);
        TextView jogging = (TextView) findViewById(R.id.txt4);

        pushup.setText(text1);
        situp.setText(text2);
        jumping_jacks.setText(text3);
        jogging.setText(text4);

        pushup.setVisibility(View.VISIBLE);
        situp.setVisibility(View.VISIBLE);
        jumping_jacks.setVisibility(View.VISIBLE);
        jogging.setVisibility(View.VISIBLE);

        switch(sport){
            case "pushup":
                pushup.setVisibility(View.GONE);
                break;
            case "situp":
                situp.setVisibility(View.GONE);
                break;
            case "jumping jacks":
                jumping_jacks.setVisibility(View.GONE);
                break;
            case "jogging":
                jogging.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }


}