package com.motiondetect.detector.model;

import java.io.Serializable;

public class BtDeviceModel implements Serializable {
    public String name;
    public String mac;

    public BtDeviceModel() {
        name = "";
        mac = "";
    }

    public BtDeviceModel(String name, String mac) {
        this.name = name;
        this.mac = mac;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BtDeviceModel that = (BtDeviceModel) o;
        return name.equals(that.name) && mac.equals(that.mac);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + mac.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("BtDeviceModel = { name=%s, mac=%s }", name, mac);
    }
}
