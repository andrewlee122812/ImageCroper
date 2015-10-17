package com.ilikelabs.imageCroper;

/**
 * Created by taXer on 15/7/9.
 */

//package com.nomnom.common;

import android.graphics.PointF;

/**
 * Convenience library to do vector calculations
 *
 * @author garysoed
 */
public class Vector {

    private float x;
    private float y;

    public Vector(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setXY(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float length() {
        return (float)Math.sqrt(lengthSquared());
    }

    public float lengthSquared() {
        return this.dot(this);
    }

    public float dot(Vector v2) {
        return x * v2.x + y * v2.y;
    }

    public Vector add(Vector v2) {
        setXY(x + v2.x, y + v2.y);
        return this;
    }

    public Vector substract(Vector v2) {
        setXY(x - v2.x, y - v2.y);
        return this;
    }

    public Vector multiply(float constant) {
        setXY(x * constant, y * constant);
        return this;
    }

    public Vector unitVector() {
        float length = length();
        return this.multiply(1 / length);
    }

    public Vector normal() {
        setXY(-y, x);
        return this;
    }

    public float bearing() {
        return (float) Math.atan2(y, x);
    }

    public static Vector copyOf(Vector vector) {
        return new Vector(vector.x, vector.y);
    }

    public static Vector fromPointF(PointF point) {
        return new Vector(point.x, point.y);
    }
}
