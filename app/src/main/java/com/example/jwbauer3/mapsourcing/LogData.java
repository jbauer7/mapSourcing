package com.example.jwbauer3.mapsourcing;

/**
 * Created by jwbauer3 on 10/21/15.
 */
public class LogData {
    private float steps;
    private float direction;
    private float pressure;
    private long timeStamp;

    public LogData(float steps, float direction, float pressure, long timeStamp){
        this.steps=steps;
        this.direction=direction;
        this.pressure=pressure;
        this.timeStamp=timeStamp;
    }

    public float getSteps(){return steps;}
    public float getDirection(){return direction;}
    public float getPressure(){return pressure;}
    public long getTimeStamp(){return timeStamp;}

}
