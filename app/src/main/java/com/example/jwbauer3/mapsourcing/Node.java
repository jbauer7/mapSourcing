package com.example.jwbauer3.mapsourcing;

/**
 * Created by Nikhil on 10/11/2015.
 */
public class Node {

    private int xPos, yPos;
    public Node(int xPos, int yPos){
        this.xPos = xPos;
        this.yPos = yPos;
    }
    public int getxPos(){
        return xPos;
    }
    public int getyPos(){
        return  yPos;
    }
    //could store radius, color, pixel area coverage.
}
