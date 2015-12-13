package com.example.jwbauer3.mapsourcing;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by njaunich on 12/12/15.
 */
public class Persistence {

    //Persistence variables
    //private static SharedPreferences sharedPreferences;
    private static String FLOOR_PREFS = "floor_";

    private SharedPreferences sharedPreferences;

    protected int floor_numOfEdges;
    protected int floor_numOfNodes;

    protected Floor floor;

    protected static Context context;

    private String PREF_NAME;

    protected HashMap<String, Node> nodeHashMap;

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
            /* Toast.makeText(context, "sharedPreferences get failed",
                    Toast.LENGTH_SHORT).show(); */
            return;
        } else {
            /* Toast.makeText(context, "sharedPreferences get successful",
                    Toast.LENGTH_SHORT).show(); */
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

    public Floor returnSavedFloor()
    {
        return floor;
    }

    public ArrayList<Node> getFloorNodes()
    {
        ArrayList<Node> nodes = new ArrayList<Node>();
        if (floor_numOfNodes == 0)
        {
            return null;
        }

        Gson gson = new Gson();
        for (int i = 0; i < floor_numOfNodes; i++)
        {
            String nodePrefId = "node_" + i;
            String nodeString = sharedPreferences.getString(nodePrefId, "");
            if (nodeString.length() > 0)
            {
                Node node = gson.fromJson(nodeString, Node.class);
                //Node node = (Node) objectDeserializer(nodeString);
                nodes.add(node);
            }
        }
        return nodes;
    }
    public BaseNode getSpecifcNode(String nodeRefString)
    {
        if (floor_numOfNodes == MainActivity.currFloor.nodes.size()
                && floor_numOfEdges == MainActivity.currFloor.edges.size()
                && nodeHashMap != null && nodeHashMap.get(nodeRefString) != null)
        {
            return nodeHashMap.get(nodeRefString);
        }
        String nodeHashMapString = sharedPreferences.getString("nodeHashMap", "");
        Gson gson = new Gson();
        Type stringNodeMap = new TypeToken<HashMap<String, Node>>(){}.getType();
        nodeHashMap = gson.fromJson(nodeHashMapString, stringNodeMap);
        return nodeHashMap.get(nodeRefString);
    }

    public boolean isFloorSaved()
    {
        if (floor_numOfEdges == 0 && floor_numOfNodes == 0)
        {
            return false;
        } else {
            return true;
        }
    }

    public int getSavedFloor()
    {
        if (floor_numOfEdges == 0 && floor_numOfNodes == 0)
        {
            Toast.makeText(context, "getSavedFloor failed; floor never saved",
                    Toast.LENGTH_SHORT).show();
            return 0;
        }
        Gson gson = new Gson();

        String nodeHashMapString = sharedPreferences.getString("nodeHashMap", "");
        Type stringNodeMap = new TypeToken<HashMap<String, Node>>(){}.getType();
        HashMap<String, Node> nodeHashMap = gson.fromJson(nodeHashMapString, stringNodeMap);
        /* for (int i = 0; i < floor_numOfNodes; i++)
        {
            String nodePrefId = "node_" + i;
            String nodeString = sharedPreferences.getString(nodePrefId, "");
            if (nodeString.length() > 0)
            {
                Node node = gson.fromJson(nodeString, Node.class);
                //Node node = (Node) objectDeserializer(nodeString);
                floor.nodes.add(node);
            }
        } */
        for (Node node : nodeHashMap.values()) {
            floor.nodes.add(node);
        }

        String edgeHashMapString = sharedPreferences.getString("edgeHashMap", "");
        Type integerEdgeMap = new TypeToken<HashMap<Integer, Edge>>(){}.getType();
        HashMap<Integer, Edge> edgeHashMap = gson.fromJson(edgeHashMapString, integerEdgeMap);
        for (int i = 0; i < floor_numOfEdges; i++)
        {
            /*String edgePrefId = "edge_" + i;
            String edgeString = sharedPreferences.getString(edgePrefId, "");
            if (edgeString.length() > 0)
            {
                Edge edge = gson.fromJson(edgeString, Edge.class);
                //Edge edge = (Edge) objectDeserializer(edgeString);
                floor.edges.add(edge);
            }*/
            floor.edges.add(edgeHashMap.get(i));
        }

        //Get other saved floor variables
        floor.floorNum = Integer.parseInt(sharedPreferences.getString("floorNum", ""));
        floor.backgroundWidth = Integer.parseInt(sharedPreferences.getString("backgroundWidth", ""));
        floor.backgroundHeight = Integer.parseInt(sharedPreferences.getString("backgroundHeight", ""));
        floor.maxMeshScaleFactor = Float.parseFloat(sharedPreferences.getString("maxMeshScaleFactor", ""));
        floor.meshReferenceState = gson.fromJson(sharedPreferences.getString("meshReferenceState", ""), ReferenceState.class);
        if (floor.floorNum == 0)
        {
            //floor.backgroundImage = ResourcesCompat.getDrawable(context.getResources(), R.drawable.eh_floor2, null);
        }
        if (floor.floorNum == 1)
        {
            //floor.backgroundImage = ResourcesCompat.getDrawable(context.getResources(), R.drawable.eh_floor3, null);
        }

        //Make Toast for success notification
        /*Toast.makeText(context, "getSavedFloor successfull",
                Toast.LENGTH_SHORT).show(); */
        return 1;
    }

    public void saveFloor(Floor floorToSave) {
        Log.d("Persistence", "saveFloor");
        floor.edges = floorToSave.getEdges();
        floor.nodes = floorToSave.getNodes();

        floor_numOfEdges = floorToSave.edges.size();
        floor_numOfNodes = floorToSave.nodes.size();

        Gson gson = new Gson();
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();

        //Save floor variables
        //TODO: Save these floor vars
        //this.storeDrawable(floorToSave.backgroundImage, "backgroundImage");
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


        HashMap<String, Node> nodeHashMap = new HashMap<String, Node>();
        for (int i = 0; i < floor_numOfNodes; i++)
        {
            /*String nodePrefId = "node_" + i;
            String nodeGson = gson.toJson(floor.nodes.get(i));
            prefsEditor.putString(nodePrefId, nodeGson); */
            nodeHashMap.put(floor.nodes.get(i).nodeRefString, floor.nodes.get(i));
            //prefsEditor.putString(nodePrefId, objectSerializer(floor.nodes.get(i)));
        }
        String nodeHashMapGson = gson.toJson(nodeHashMap);
        prefsEditor.putString("nodeHashMap", nodeHashMapGson);

        HashMap<Integer, Edge> edgeHashMap = new HashMap<Integer, Edge>();
        for (int i = 0; i < floor_numOfEdges; i++)
        {
            /*String edgePrefId = "edge_" + i;
            String edgeGson = gson.toJson(floor.edges.get(i));
            prefsEditor.putString(edgePrefId, edgeGson); */
            edgeHashMap.put(i, floor.edges.get(i));
            //prefsEditor.putString(edgePrefId, objectSerializer(floor.edges.get(i)));
        }
        String edgeHashMapGson = gson.toJson(edgeHashMap);
        prefsEditor.putString("edgeHashMap", edgeHashMapGson);

        prefsEditor.commit();
        /*Toast.makeText(context, "Floor saved | Edges = " + floor_numOfEdges + " Nodes = " + floor_numOfNodes,
                Toast.LENGTH_SHORT).show(); */
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