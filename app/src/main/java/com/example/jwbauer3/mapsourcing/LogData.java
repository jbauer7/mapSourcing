package com.example.jwbauer3.mapsourcing;

/**
 * Created by jwbauer3 on 10/21/15.
 */
public class LogData {
    private float steps;
    private float direction;
    //private float pressure;
    private long timeStamp;
    //private float[] acc, rot;


    public LogData(float steps, float direction, long timeStamp){
        this.steps=steps;
        this.direction=direction;
        //this.pressure=pressure;
        this.timeStamp=timeStamp;
        //this.acc = new float[acc.length];
        //System.arraycopy(acc, 0, this.acc, 0, acc.length);
        //this.rot = new float[rot.length];
        //System.arraycopy(rot, 0, this.rot, 0, rot.length);
    }

    public float getSteps(){return steps;}
    public float getDirection(){return direction;}
    //public float getPressure(){return pressure;}
    public long getTimeStamp(){return timeStamp;}
    //public float[] getAcc(){return acc;}
    //public float[] getRot(){return rot;}
}
