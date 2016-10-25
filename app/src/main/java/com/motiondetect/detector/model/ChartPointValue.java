package com.motiondetect.detector.model;

public class ChartPointValue {
    public int x;
    public float y;

    public ChartPointValue(int x, float y) {
        this.x = x;
        this.y = y;
    }

    public void reset() {
        x = 0;
        y = 0f;
    }
}
