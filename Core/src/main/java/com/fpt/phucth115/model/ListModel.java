package com.fpt.phucth115.model;

import java.util.ArrayList;

public abstract class ListModel<T extends BaseModel> implements BaseModel {
    private ArrayList<T> data;

    public ArrayList<T> getData() {
        return data;
    }

    public void setData(ArrayList<T> data) {
        this.data = data;
    }
}
