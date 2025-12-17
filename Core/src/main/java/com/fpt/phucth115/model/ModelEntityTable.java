package com.fpt.phucth115.model;

import java.util.ArrayList;

public interface ModelEntityTable<T extends ModelEntityInstance> extends BaseModel {
    ArrayList<T> getAll();
    T getById(int id);
}
