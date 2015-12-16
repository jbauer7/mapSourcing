package com.example.jwbauer3.mapsourcing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class MainMenuActivity extends Activity {

    private String[] buildingNames = {"Engineering Hall", "Computer Aided Engineering", "Engineering Centers Building"};

    private static boolean presentationMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        setUpSpinner();
    }

    public void navigatePressed(View view){
        //start activity with mapping enabled
        Intent mapIntent = new Intent(this, MainActivity.class);
        mapIntent.putExtra("mode", "navigation");
        if (presentationMode)
        {
            mapIntent.putExtra("presentationMode", "true");
        } else {
            mapIntent.putExtra("presentationMode", "false");
        }
        startActivity(mapIntent);
    }
    public void mapPressed(View view){
        //start activity with mapping disabled.
        Intent mapIntent = new Intent(this, MainActivity.class);
        mapIntent.putExtra("mode", "map");
        if (presentationMode)
        {
            mapIntent.putExtra("presentationMode", "true");
        } else {
            mapIntent.putExtra("presentationMode", "false");
        }
        startActivity(mapIntent);
    }
    public void addBuilding(View view){
        Toast.makeText(this, "Not enabled", Toast.LENGTH_SHORT).show();
    }
    public void presentationModeToggle(View view){
        if (!presentationMode)
        {
            presentationMode = true;
            Toast.makeText(this, "Presentation Mode Enabled", Toast.LENGTH_SHORT).show();
        } else {
            presentationMode = false;
            Toast.makeText(this, "Presentation Mode Disabled", Toast.LENGTH_SHORT).show();
        }
    }

    private void setUpSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.Spinner_MainMenu_BuildingSelect);
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, buildingNames);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //does nothing atm
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });
    }

}
