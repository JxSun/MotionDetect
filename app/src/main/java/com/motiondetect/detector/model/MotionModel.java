package com.motiondetect.detector.model;

public class MotionModel {
    public long timestamp = 0;
    public float accX = 0f;
    public float accY = 0f;
    public float accZ = 0f;
    public float gyroX = 0f;
    public float gyroY = 0f;
    public float gyroZ = 0f;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("timestamp=").append(timestamp);
        builder.append(", acc=[").append(accX).append(",").append(accY).append(",").append(accZ).append("]");
        builder.append(", gyro=[").append(gyroX).append(",").append(gyroY).append(",").append(gyroZ).append("]");
        return builder.toString();
    }
}
