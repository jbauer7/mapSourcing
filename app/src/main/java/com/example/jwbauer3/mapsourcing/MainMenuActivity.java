package com.example.jwbauer3.mapsourcing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class MainMenuActivity extends Activity {

    ArrayAdapter buildingAdapter;
    private ArrayList<Building> buildings;
    private long selectedBuildingId;

    private static boolean presentationMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        setUpSpinner();
    }

    public void navigatePressed(View view) {
        //start activity with mapping enabled
        Intent mapIntent = new Intent(this, MainActivity.class);
        mapIntent.putExtra("mode", "navigation");
        mapIntent.putExtra("selectedBuildingId", selectedBuildingId);
        if (presentationMode) {
            mapIntent.putExtra("presentationMode", "true");
        } else {
            mapIntent.putExtra("presentationMode", "false");
        }
        startActivity(mapIntent);
    }

    public void mapPressed(View view) {
        //start activity with mapping disabled.
        Intent mapIntent = new Intent(this, MainActivity.class);
        mapIntent.putExtra("mode", "map");
        mapIntent.putExtra("selectedBuildingId", selectedBuildingId);
        if (presentationMode) {
            mapIntent.putExtra("presentationMode", "true");
        } else {
            mapIntent.putExtra("presentationMode", "false");
        }
        startActivity(mapIntent);
    }

    public void addBuilding(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add a Building");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String buildingName = input.getText().toString();
                selectedBuildingId = DatabaseHelper.addBuilding(buildingName);
                buildings.add(new Building(selectedBuildingId, buildingName));
                buildingAdapter.notifyDataSetChanged();
                Spinner spinner = (Spinner) findViewById(R.id.Spinner_MainMenu_BuildingSelect);
                spinner.setSelection(buildings.size() - 1, true);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void presentationModeToggle(View view) {
        if (!presentationMode) {
            presentationMode = true;
            Toast.makeText(this, "Presentation Mode Enabled", Toast.LENGTH_SHORT).show();
        } else {
            presentationMode = false;
            Toast.makeText(this, "Presentation Mode Disabled", Toast.LENGTH_SHORT).show();
        }
    }

    private void setUpSpinner() {
        //Load the buildings
        buildings = DatabaseHelper.getBuildings();

        //Set up defaults
        if (buildings.size() == 0) {
            long tempId = DatabaseHelper.addBuilding("Engineering Hall");
            buildings.add(new Building(tempId, "Engineering Hall"));
            tempId = DatabaseHelper.addBuilding("Computer Aided Engineering");
            buildings.add(new Building(tempId, "Computer Aided Engineering"));
            tempId = DatabaseHelper.addBuilding("Engineering Centers Building");
            buildings.add(new Building(tempId, "Engineering Centers Building"));
        }

        //Set the first building as selected
        selectedBuildingId = buildings.get(0).getId();

        //Create an adapter
        buildingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, buildings);

        Spinner spinner = (Spinner) findViewById(R.id.Spinner_MainMenu_BuildingSelect);
        spinner.setAdapter(buildingAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //does nothing atm
                selectedBuildingId = buildings.get(position).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });
    }

}
