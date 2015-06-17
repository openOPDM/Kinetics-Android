package org.kineticsfoundation.lib;

public class DataItem {

    public double ts;
    public float[] values;

    public DataItem() {
    }

    public DataItem(double ts, float[] values) {
        this.ts = ts;
        this.values = values;
    }

}
