package com.navitend.ble1;

import java.util.ArrayList;

public class SampleData {
    public ArrayList<Float> data_y;
    public ArrayList<Integer> data_x;

    public SampleData() {
        this.data_y = new ArrayList<Float>();
        this.data_x = new ArrayList<Integer>();
    }
    public SampleData(ArrayList<Float> data_y,ArrayList<Integer> data_x) {
        this.data_y = data_y;
        this.data_x = data_x;
    }
}
