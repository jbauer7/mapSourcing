package com.example.jwbauer3.mapsourcing;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "mapSourcing.db";
    private static final String BUILDING_TABLE = "Buildings";
    private static final String FLOOR_TABLE = "Floors";
    private static final String REFERENCE_STATE_TABLE = "ReferenceStates";
    private static final String NODE_TABLE = "Nodes";
    private static final String EDGE_TABLE = "Edges";

    //int buildingId
    //String buildingName
    private static final String CREATE_BUILDING_TABLE_QUERY = "CREATE TABLE " + BUILDING_TABLE +
            " (" +
            "buildingId INTEGER PRIMARY KEY, " +
            "buildingName TEXT" +
            ");";

    //int floorId
    //int buildingId
    //int floorNum
    //int backgroundImageResId
    //float maxMeshScaleFactor
    private static final String CREATE_FLOOR_TABLE_QUERY = "CREATE TABLE " + FLOOR_TABLE +
            " (" +
            "floorId INTEGER PRIMARY KEY, " +
            "buildingId INTEGER, " +
            "floorNum INTEGER, " +
            "backgroundImageResId INTEGER, " +
            "maxMeshScaleFactor REAL" +
            ");";

    //int referenceStateId
    //int floorId
    //float startX;
    //float startY;
    //float transX;
    //float transY;
    //float prevTransX;
    //float prevTransY;
    //float scaleFactor;
    private static final String CREATE_REFERENCE_STATE_TABLE_QUERY = "CREATE TABLE " + REFERENCE_STATE_TABLE +
            " (" +
            "referenceStateId INTEGER PRIMARY KEY, " +
            "floorId INTEGER, " +
            "startX REAL, " +
            "startY REAL, " +
            "transX REAL, " +
            "transY REAL, " +
            "prevTransX REAL, " +
            "prevTransY REAL, " +
            "scaleFactor REAL" +
            ");";

    //int nodeId
    //int floorId
    //int defaultXPos
    //int defaultYPos
    //int xPos
    //int yPos
    //boolean isStairNode
    private static final String CREATE_NODE_TABLE_QUERY = "CREATE TABLE " + NODE_TABLE +
            " (" +
            "nodeId INTEGER PRIMARY KEY, " +
            "floorId INTEGER, " +
            "defaultXPos INTEGER, " +
            "defaultYPos INTEGER, " +
            "xPos INTEGER, " +
            "yPos INTEGER, " +
            "isStairNode INTEGER" +
            ");";

    //int edgeId
    //int floorId
    //int startNodeId
    //int endNodeId
    private static final String CREATE_EDGE_TABLE_QUERY = "CREATE TABLE " + EDGE_TABLE +
            " (" +
            "edgeId INTEGER PRIMARY KEY, " +
            "floorId INTEGER, " +
            "startNodeId INTEGER, " +
            "endNodeId INTEGER, " +
            "weight INTEGER, " +
            "direction INTEGER" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BUILDING_TABLE_QUERY);
        db.execSQL(CREATE_FLOOR_TABLE_QUERY);
        db.execSQL(CREATE_REFERENCE_STATE_TABLE_QUERY);
        db.execSQL(CREATE_NODE_TABLE_QUERY);
        db.execSQL(CREATE_EDGE_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + EDGE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + NODE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + REFERENCE_STATE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + FLOOR_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + BUILDING_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onUpgrade(db, oldVersion, newVersion);
    }

    private static void saveNodes(SQLiteDatabase db, long floorId, ArrayList<Node> nodes) {
        for (Node node : nodes) {
            //Insert the Node
            ContentValues row = new ContentValues();
            row.put("floorId", floorId);
            row.put("defaultXPos", node.getDefaultXPos());
            row.put("defaultYPos", node.getDefaultYPos());
            row.put("xPos", node.getxPos());
            row.put("yPos", node.getyPos());
            row.put("isStairNode", node.getIsStairNode());

            //Commit and update the node.id
            node.id = db.insertWithOnConflict(NODE_TABLE, null, row, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    private static void saveEdges(SQLiteDatabase db, long floorId, ArrayList<Edge> edges) {
        for (Edge edge : edges) {
            //Insert the Node
            ContentValues row = new ContentValues();
            row.put("floorId", floorId);
            row.put("startNodeId", edge.getStart().id);
            row.put("endNodeId", edge.getEnd().id);
            row.put("weight", edge.getWeight());
            row.put("direction", edge.getDirection());
            db.insertWithOnConflict(EDGE_TABLE, null, row, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    private static void removeFloor(SQLiteDatabase db, long floorId) {
        db.execSQL("DELETE FROM " + EDGE_TABLE + " WHERE floorId = ?",
                new String[]{Long.toString(floorId)});
        db.execSQL("DELETE FROM " + NODE_TABLE + " WHERE floorId = ?",
                new String[]{Long.toString(floorId)});
        db.execSQL("DELETE FROM " + REFERENCE_STATE_TABLE + " WHERE floorId = ?",
                new String[]{Long.toString(floorId)});
        db.execSQL("DELETE FROM " + FLOOR_TABLE + " WHERE floorId = ?",
                new String[]{Long.toString(floorId)});
    }

    public static void saveFloor(long buildingId, Floor floor)
    {
        DatabaseHelper databaseHelper = new DatabaseHelper(Application.getContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        Cursor cur = db.rawQuery("SELECT * FROM " + FLOOR_TABLE +
                        " WHERE buildingId = ? AND" +
                        " floorNum = ?",
                new String[]{Long.toString(buildingId),
                        Integer.toString(floor.getFloorNum())});

        //Delete old Floor (or multiple if there are more somehow)
        long floorId = -1;
        while(cur.moveToNext()) {
            floorId = cur.getLong(cur.getColumnIndex("floorId"));
            removeFloor(db, floorId);
        }

        //Close the cursor
        cur.close();

        //Insert the Floor
        ContentValues row = new ContentValues();
        if (floorId != -1) {
            //If we have an old id, replace it, otherwise one will be generated
            row.put("floorId", floorId);
        }
        row.put("buildingId", buildingId);
        row.put("floorNum", floor.getFloorNum());
        row.put("backgroundImageResId", floor.getBackgroundImageResId());
        row.put("maxMeshScaleFactor", floor.maxMeshScaleFactor);
        long newFloorId = db.insertWithOnConflict(FLOOR_TABLE, null, row, SQLiteDatabase.CONFLICT_REPLACE);

        //Insert the ReferenceState
        row = new ContentValues();
        row.put("floorId", newFloorId);
        row.put("startX", floor.meshReferenceState.startX);
        row.put("startY", floor.meshReferenceState.startY);
        row.put("transX", floor.meshReferenceState.transX);
        row.put("transY", floor.meshReferenceState.transY);
        row.put("prevTransX", floor.meshReferenceState.prevTransX);
        row.put("prevTransY", floor.meshReferenceState.prevTransY);
        row.put("scaleFactor", floor.meshReferenceState.scaleFactor);
        db.insertWithOnConflict(REFERENCE_STATE_TABLE, null, row, SQLiteDatabase.CONFLICT_REPLACE);

        //Save the nodes and edges (nodes must be done first)
        DatabaseHelper.saveNodes(db, newFloorId, floor.nodes);
        DatabaseHelper.saveEdges(db, newFloorId, floor.edges);

        db.close();
    }

    public static long addBuilding(String buildingName) {
        DatabaseHelper databaseHelper = new DatabaseHelper(Application.getContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        //Insert the building
        ContentValues row = new ContentValues();
        row.put("buildingName", buildingName);
        long result = db.insertWithOnConflict(BUILDING_TABLE, null, row, SQLiteDatabase.CONFLICT_REPLACE);

        db.close();

        return result;
    }

    public static void removeBuilding(long buildingId) {
        DatabaseHelper databaseHelper = new DatabaseHelper(Application.getContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        Cursor cur = db.rawQuery("SELECT * FROM " + FLOOR_TABLE +
                        " WHERE buildingId = ?",
                new String[]{Long.toString(buildingId)});

        //Delete old Floor (or multiple if there are more somehow)
        while(cur.moveToNext()) {
            removeFloor(db, cur.getLong(cur.getColumnIndex("floorId")));
        }

        //Close the cursor
        cur.close();

        //Actually delete the building
        db.execSQL("DELETE FROM " + BUILDING_TABLE + " WHERE buildingId = " + Long.toString(buildingId));

        db.close();
    }

    private static HashMap<Long, Node> nodeHashMap = null;
    private static ArrayList<Node> getNodes(SQLiteDatabase db, long floorId, int floorNum, float scaleFactor) {
        ArrayList<Node> result = new ArrayList<>();

        Cursor cur = db.rawQuery("SELECT * FROM " + NODE_TABLE +
                        " WHERE floorId = ?",
                new String[]{Long.toString(floorId)});

        nodeHashMap = new HashMap<>();

        while (cur.moveToNext()) {
            long nodeId = cur.getLong(cur.getColumnIndex("nodeId"));
            int defaultXPos = cur.getInt(cur.getColumnIndex("defaultXPos"));
            int defaultYPos = cur.getInt(cur.getColumnIndex("defaultYPos"));
            int xPos = cur.getInt(cur.getColumnIndex("xPos"));
            int yPos = cur.getInt(cur.getColumnIndex("yPos"));
            boolean isStairNode = (cur.getInt(cur.getColumnIndex("isStairNode")) != 0);

            Node temp = new Node(nodeId,
                    defaultXPos,
                    defaultYPos,
                    xPos,
                    yPos,
                    isStairNode,
                    floorNum);
            temp.setScaleFactor(scaleFactor);
            nodeHashMap.put(nodeId, temp);
            result.add(temp);
        }

        //Close the cursor
        cur.close();

        return result;
    }

    private static ArrayList<Edge> getEdges(SQLiteDatabase db, long floorId, float scaleFactor) {
        ArrayList<Edge> result = new ArrayList<>();

        Cursor cur = db.rawQuery("SELECT * FROM " + EDGE_TABLE +
                        " WHERE floorId = ?",
                new String[]{Long.toString(floorId)});

        while(cur.moveToNext()) {
            long startNodeId = cur.getLong(cur.getColumnIndex("startNodeId"));
            long endNodeId = cur.getLong(cur.getColumnIndex("endNodeId"));
            int weight = cur.getInt(cur.getColumnIndex("weight"));
            int direction = cur.getInt(cur.getColumnIndex("direction"));

            Node startNode = nodeHashMap.get(startNodeId);
            Node endNode = nodeHashMap.get(endNodeId);

            Edge temp = new Edge(startNode,
                    endNode,
                    weight,
                    direction);
            startNode.addEdge(temp);
            endNode.addEdge(temp);
            temp.setScaleFactor(scaleFactor);
            result.add(temp);
        }

        //Close the cursor
        cur.close();

        return result;
    }

    private static Floor getFloorFromCursor(SQLiteDatabase db, Cursor cur, int floorNum) {
        long floorId = cur.getLong(cur.getColumnIndex("floorId"));
        int backgroundImageResId = cur.getInt(cur.getColumnIndex("backgroundImageResId"));
        float maxMeshScaleFactor = cur.getFloat(cur.getColumnIndex("maxMeshScaleFactor"));

        Floor result = new Floor();
        result.floorNum = floorNum;
        result.backgroundImageResId = backgroundImageResId;
        result.maxMeshScaleFactor = maxMeshScaleFactor;

        //Get the ReferenceState
        Cursor refCur = db.rawQuery("SELECT * FROM " + REFERENCE_STATE_TABLE +
                        " WHERE floorId = ?",
                new String[]{Long.toString(floorId)});

        result.meshReferenceState = new ReferenceState();
        if(refCur.moveToNext()) {
            float startX = refCur.getFloat(refCur.getColumnIndex("startX"));
            float startY = refCur.getFloat(refCur.getColumnIndex("startY"));
            float transX = refCur.getFloat(refCur.getColumnIndex("transX"));
            float transY = refCur.getFloat(refCur.getColumnIndex("transY"));
            float prevTransX = refCur.getFloat(refCur.getColumnIndex("prevTransX"));
            float prevTransY = refCur.getFloat(refCur.getColumnIndex("prevTransY"));
            float scaleFactor = refCur.getFloat(refCur.getColumnIndex("scaleFactor"));

            result.meshReferenceState.startX = startX;
            result.meshReferenceState.startY = startY;
            result.meshReferenceState.transX = transX;
            result.meshReferenceState.transY = transY;
            result.meshReferenceState.prevTransX = prevTransX;
            result.meshReferenceState.prevTransY = prevTransY;
            result.meshReferenceState.scaleFactor = scaleFactor;
        }

        //Close the cursor
        refCur.close();

        result.nodes = DatabaseHelper.getNodes(db, floorId, floorNum, result.meshReferenceState.scaleFactor);
        result.edges = DatabaseHelper.getEdges(db, floorId, result.meshReferenceState.scaleFactor);

        return result;
    }

    public static Floor getFloor(long buildingId, int floorNum)
    {
        DatabaseHelper databaseHelper = new DatabaseHelper(Application.getContext());
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cur = db.rawQuery("SELECT * FROM " + FLOOR_TABLE +
                        " WHERE buildingId = ? AND" +
                        " floorNum = ?",
                new String[]{Long.toString(buildingId),
                        Integer.toString(floorNum)});

        //Get the floor
        Floor result = null;
        if(cur.moveToNext()) {
            result = getFloorFromCursor(db, cur, floorNum);
        }

        //Hmmmm, there shouldn't be the same floor twice for a building
        if(cur.moveToNext()) {
            System.err.println("Multiple Floors Defined, Returned First");
        }

        //Close the cursor and database
        cur.close();
        db.close();

        return result;
    }

    public static ArrayList<Floor> getAllFloors(long buildingId) {
        DatabaseHelper databaseHelper = new DatabaseHelper(Application.getContext());
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cur = db.rawQuery("SELECT * FROM " + FLOOR_TABLE +
                        " WHERE buildingId = ?",
                new String[]{Long.toString(buildingId)});

        ArrayList<Floor> result = new ArrayList<>();

        while (cur.moveToNext()) {
            int floorNum = cur.getInt(cur.getColumnIndex("floorNum"));
            result.add(getFloorFromCursor(db, cur, floorNum));
        }

        //Close the cursor
        cur.close();

        return result;
    }

    public static ArrayList<Building> getBuildings() {
        DatabaseHelper databaseHelper = new DatabaseHelper(Application.getContext());
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        Cursor cur = db.rawQuery("SELECT * FROM " + BUILDING_TABLE, new String[]{});

        ArrayList<Building> result = new ArrayList<>();

        while (cur.moveToNext()) {
            long id = cur.getLong(cur.getColumnIndex("buildingId"));
            String name = cur.getString(cur.getColumnIndex("buildingName"));
            result.add(new Building(id, name));
        }

        //Close the cursor
        cur.close();

        return result;
    }
}

