package com.google.android.gms.samples.vision.face.facetracker;

/**
 * Created by amritkaur on 05-05-2017.
 */

public class Coordinate {
    private float x;
    private float y;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public static double  distanceBetweenCoordinates(Coordinate pointA, Coordinate pointB) {
        return Math.sqrt((Math.pow(pointB.x - pointA.x , 2) + Math.pow(pointB.y - pointA.y, 2)));
    }
}
