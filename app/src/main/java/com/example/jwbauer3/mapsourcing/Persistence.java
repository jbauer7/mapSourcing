package com.example.jwbauer3.mapsourcing;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by njaunich on 12/12/15.
 */
public class Persistence {

    //Persistence variables
    //private static SharedPreferences sharedPreferences;
    private static String FLOOR_PREFS = "floor_";

    private SharedPreferences sharedPreferences;

    private int floor_numOfEdges;
    private int floor_numOfNodes;

    private Floor floor;

    private Context context;

    private String PREF_NAME;

    public Persistence(Context context, int type, String building, int floorNumber)
    {
        this.context = context;
        floor = new Floor();
        //if type == 1 then persistence is init'ed as floor
        PREF_NAME = "";
        if (type == 1) {
            PREF_NAME = building + "_floor_" + floorNumber;
        }

        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences == null)
        {
            Toast.makeText(context, "sharedPreferences get failed",
                    Toast.LENGTH_SHORT).show();
            return;
        } else {
            Toast.makeText(context, "sharedPreferences get successful",
                    Toast.LENGTH_SHORT).show();
        }
        String numOfEdges = sharedPreferences.getString("numOfEdges", "");
        String numOfNodes = sharedPreferences.getString("numOfNodes", "");

        if (numOfNodes.length() > 0 && numOfEdges.length() > 0) {
            floor_numOfEdges = Integer.parseInt(numOfEdges);
            floor_numOfNodes = Integer.parseInt(numOfNodes);
        } else {
            Toast.makeText(context, "Get saved floor failed; floor never saved",
                    Toast.LENGTH_SHORT).show();
        }

    }

    public void getSavedFloor()
    {
        if (floor_numOfEdges == 0 && floor_numOfNodes == 0)
        {
            Toast.makeText(context, "getSavedFloor failed; floor never saved",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Gson gson = new Gson();
        for (int i = 0; i < floor_numOfNodes; i++)
        {
            String nodePrefId = "node_" + i;
            String nodeString = sharedPreferences.getString(nodePrefId, "");
            if (nodeString.length() > 0)
            {
                Node node = gson.fromJson(nodeString, Node.class);
                floor.nodes.add(node);
            }
        }
        for (int i = 0; i < floor_numOfEdges; i++)
        {
            String edgePrefId = "edge_" + i;
            String edgeString = sharedPreferences.getString(edgePrefId, "");
            if (edgeString.length() > 0)
            {
                Edge edge = gson.fromJson(edgeString, Edge.class);
                floor.edges.add(edge);
            }
        }

        //Get other saved floor variables
        floor.floorNum = Integer.parseInt(sharedPreferences.getString("floorNum", ""));
        floor.backgroundWidth = Integer.parseInt(sharedPreferences.getString("backgroundWidth", ""));
        floor.backgroundHeight = Integer.parseInt(sharedPreferences.getString("backgroundHeight", ""));
        floor.maxMeshScaleFactor = Float.parseFloat(sharedPreferences.getString("maxMeshScaleFactor", ""));
        floor.meshReferenceState = gson.fromJson(sharedPreferences.getString("meshReferenceState", ""), ReferenceState.class);
        floor.backgroundImage = this.getDrawable("backgroundImage");

        //Make Toast for success notification
        Toast.makeText(context, "getSavedFloor successfull",
                Toast.LENGTH_SHORT).show();
    }

    private void storeDrawable(Drawable drawable, String drawableName) {
        Bitmap drawableBitmap = ((BitmapDrawable) drawable).getBitmap();
        File pictureFile = getOutputMediaFile(drawableName);

        if (pictureFile == null) {
            Log.d("Persistence",
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            drawableBitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("Persistence", "Image file not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("Persistence", "Error accessing image file: " + e.getMessage());
        }
    }

    private Drawable getDrawable(String drawableName) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + context.getPackageName()
                + "/Files");
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        String mImageName = PREF_NAME + drawableName + ".jpg";
        Bitmap image = BitmapFactory.decodeFile(mediaStorageDir.getPath() + File.separator + mImageName);
        Drawable drawable = new BitmapDrawable(image);
        return drawable;
    }

    private File getOutputMediaFile(String drawableName){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + context.getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        File mediaFile;
        String mImageName = PREF_NAME + drawableName + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public void saveFloor(Floor floorToSave) {
        floor.edges = floorToSave.getEdges();
        floor.nodes = floorToSave.getNodes();

        floor_numOfEdges = floorToSave.edges.size();
        floor_numOfNodes = floorToSave.nodes.size();

        Gson gson = new Gson();
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();

        //Save floor variables
        //TODO: Save these floor vars
        //Bitmap backgroundImage = ((BitmapDrawable) floorToSave.backgroundImage).getBitmap();
        this.storeDrawable(floorToSave.backgroundImage, "backgroundImage");
        //private Drawable backgroundImage;

        prefsEditor.putString("meshReferenceState", gson.toJson(floorToSave.meshReferenceState));
        prefsEditor.putString("floorNum", "" + floorToSave.floorNum);
        //private int backgroundWidth; //image
        prefsEditor.putString("backgroundWidth", "" + floorToSave.backgroundWidth);
        //private int backgroundHeight; //image
        prefsEditor.putString("backgroundHeight", "" + floorToSave.backgroundHeight);
        //private float maxMeshScaleFactor = -1f;
        prefsEditor.putString("maxMeshScaleFactor", "" + floorToSave.maxMeshScaleFactor);
        prefsEditor.putString("numOfEdges", "" + floor_numOfEdges);
        prefsEditor.putString("numOfNodes", "" + floor_numOfNodes);
        prefsEditor.commit();

        for (int i = 0; i < floor_numOfNodes; i++)
        {
            String nodePrefId = "node_" + i;
            String nodeGson = gson.toJson(floor.nodes.get(i));
            prefsEditor.putString(nodePrefId, nodeGson);
            prefsEditor.commit();
        }
        for (int i = 0; i < floor_numOfEdges; i++)
        {
            String edgePrefId = "edge_" + i;
            String edgeGson = gson.toJson(floor.edges.get(i));
            prefsEditor.putString(edgePrefId, edgeGson);
            prefsEditor.commit();
        }
        Toast.makeText(context, "Floor saved | Edges = " + floor_numOfEdges + " Nodes = " + floor_numOfNodes,
                Toast.LENGTH_SHORT).show();
    }

    /* private static void saveEdge(String floorName, Floor floor) {

    }

    public static void initPersistence(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    public static void saveFloor(String floorName, Floor floor) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(floor);
        prefsEditor.putString(floorName, json);
        prefsEditor.commit();
    }

    public static Floor getFloor(String floorName) {
        Gson gson = new Gson();
        Floor floor = gson.fromJson(sharedPreferences.getString(floorName, ""), Floor.class);
        return floor;
    } */
}