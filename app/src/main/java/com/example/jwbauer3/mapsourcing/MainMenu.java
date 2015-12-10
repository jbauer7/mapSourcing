package com.example.jwbauer3.mapsourcing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainMenu extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    public void navigatePressed(View view){
        //start activity with mapping enabled
        Intent mapIntent = new Intent(this, MainActivity.class);
        mapIntent.putExtra("mode", "navigation");
        startActivity(mapIntent);
    }
    public void mapPressed(View view){
        //start activity with mapping disabled.
        Intent mapIntent = new Intent(this, MainActivity.class);
        mapIntent.putExtra("mode", "map");
        startActivity(mapIntent);
    }
    public void addBuilding(View view){
        Toast.makeText(this, "Not enabled", Toast.LENGTH_SHORT).show();
    }
}
