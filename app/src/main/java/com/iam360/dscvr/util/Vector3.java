package com.iam360.dscvr.util;

//http://alvinalexander.com/java/jwarehouse/apps-for-android/HeightMapProfiler/src/com/android/heightmapprofiler/Vector3.java.shtml

public class Vector3 {
    public float x;
    public float y;
    public float z;

    public static final Vector3 ZERO = new Vector3(0, 0, 0);

    public Vector3() {
    }

    public Vector3(float xValue, float yValue, float zValue) {
        set(xValue, yValue, zValue);
    }

    public Vector3(Vector3 other) {
        set(other);
    }

    public final void add(Vector3 other) {
        x += other.x;
        y += other.y;
        z += other.z;
    }

    public final void add(float otherX, float otherY, float otherZ) {
        x += otherX;
        y += otherY;
        z += otherZ;
    }

    public final void subtract(Vector3 other) {
        x -= other.x;
        y -= other.y;
        z -= other.z;
    }

    public final void multiply(float magnitude) {
        x *= magnitude;
        y *= magnitude;
        z *= magnitude;
    }

    public final void multiply(Vector3 other) {
        x *= other.x;
        y *= other.y;
        z *= other.z;
    }

    public final void divide(float magnitude) {
        if (magnitude != 0.0f) {
            x /= magnitude;
            y /= magnitude;
            z /= magnitude;
        }
    }

    public final void set(Vector3 other) {
        x = other.x;
        y = other.y;
        z = other.z;
    }

    public final void set(float xValue, float yValue, float zValue) {
        x = xValue;
        y = yValue;
        z = zValue;
    }

    public final float dot(Vector3 other) {
        return (x * other.x) + (y * other.y) + (z * other.z);
    }

    public final float length() {
        return (float) Math.sqrt(length2());
    }

    public final float length2() {
        return (x * x) + (y * y) + (z * z);
    }

    public final boolean isZero() {
        if(x == 0 && y == 0 && z == 0) return true;
        return false;
    }

    public final float distance2(Vector3 other) {
        float dx = x - other.x;
        float dy = y - other.y;
        float dz = z - other.z;
        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    public final float normalize() {
        final float magnitude = length();

        // TODO: I'm choosing safety over speed here.
        if (magnitude != 0.0f) {
            x /= magnitude;
            y /= magnitude;
            z /= magnitude;
        }

        return magnitude;
    }

    public final void zero() {
        set(0.0f, 0.0f, 0.0f);
    }

    @Override
    public String toString() {
        return "{x:" + x + " y:" + y + " z:" + z + "}";
    }

    // static methods for vector operations

    public final static Vector3 add(Vector3 vectorA, Vector3 vectorB) {
        return new Vector3(vectorA.x + vectorB.x, vectorA.y + vectorB.y, vectorA.z + vectorB.z);
    }

    public final static Vector3 subtract(Vector3 vectorA, Vector3 vectorB) {
        return new Vector3(vectorA.x - vectorB.x, vectorA.y - vectorB.y, vectorA.z - vectorB.z);
    }

    public final static Vector3 multiply(Vector3 vectorA, Vector3 vectorB) {
        return new Vector3(vectorA.x * vectorB.x, vectorA.y * vectorB.y, vectorA.z * vectorB.z);
    }

    public final static Vector3 multiply(Vector3 vector, float magnitude) {
        return new Vector3(vector.x * magnitude, vector.y * magnitude, vector.z * magnitude);
    }

    public final static Vector3 divide(Vector3 vectorA, Vector3 vectorB) {
        return new Vector3(vectorA.x / vectorB.x, vectorA.y / vectorB.y, vectorA.z / vectorB.z);
    }

    public final static float dot(Vector3 vectorA, Vector3 vectorB) {
        return (vectorA.x * vectorB.x) + (vectorA.y * vectorB.y) + (vectorA.z * vectorB.z);
    }

    public final static float length(Vector3 vector) {
        return (float) Math.sqrt(length2(vector));
    }

    public final static float length2(Vector3 vector) {
        return (vector.x * vector.x) + (vector.y * vector.y) + (vector.z * vector.z);
    }

    public final static float distance2(Vector3 vectorA, Vector3 vectorB) {
        float dx = vectorA.x - vectorB.x;
        float dy = vectorA.y - vectorB.y;
        float dz = vectorA.z - vectorB.z;
        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    public final static Vector3 normalize(Vector3 vector) {
        final float magnitude = length(vector);

        if (magnitude != 0.0f) {
            vector.x /= magnitude;
            vector.y /= magnitude;
            vector.z /= magnitude;
        }

        return vector;
    }

}
