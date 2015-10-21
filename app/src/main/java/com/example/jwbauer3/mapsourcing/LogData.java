package com.example.jwbauer3.mapsourcing;

/**
 * Created by jwbauer3 on 10/21/15.
 */
public class LogData {
    private float steps;
    private float direction;
    private float pressure;
    private long timeStamp;
    private float x, y, z;

    public LogData(float steps, float direction, float pressure, long timeStamp,
                   float x, float y, float z){
        this.steps=steps;
        this.direction=direction;
        this.pressure=pressure;
        this.timeStamp=timeStamp;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getSteps(){return steps;}
    public float getDirection(){return direction;}
    public float getPressure(){return pressure;}
    public long getTimeStamp(){return timeStamp;}
    public float getX(){return x;}
    public float getY(){return y;}
    public float getZ(){return z;}

}
