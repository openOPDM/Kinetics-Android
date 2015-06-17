package org.kineticsfoundation.lib;

public class FilterLibrary {

    private double jerk;

    private double rms;

    private double area;

    static {
        System.loadLibrary("kinetics-filter-lib");
    }

    public native void process(DataItem[] items);

    public double getJerk() {
        return jerk;
    }

    public double getRms() {
        return rms;
    }

    public double getArea() {
        return area;
    }

}
