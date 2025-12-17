package com.fptu.phucth115;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DBContext {
    protected Connection connection;

    public void beginTransaction() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void commitTransaction() {
        try {
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public abstract <T extends DBEntity> void insert(T entity);
    public abstract <T extends DBEntity> void update(T entity);
    public abstract <T extends DBEntity> void delete(T entity);
    public abstract <T extends DBEntity> ArrayList<T> getAll(Class<T> type);
    public abstract <T extends DBEntity> void getOne(T entity);

    protected void addParamValue(PreparedStatement statement, int index, Object value, String dataType) throws SQLException
    {
        switch(dataType)
        {
            case DBField.STRING : statement.setString(index, (String)value); break;
            case DBField.INT : statement.setInt(index, (Integer)value); break;
            case DBField.DOUBLE : statement.setDouble(index, (Double)value); break;
            case DBField.FLOAT : statement.setFloat(index, (Float)value); break;
            case DBField.BOOLEAN : statement.setBoolean(index, (Boolean)value); break;
            case DBField.DATE : statement.setDate(index, (Date)value); break;
        }
    }

    protected <T extends DBEntity> void doORM(ResultSet rs, T entity) throws SQLException
    {
        Class type = entity.getClass();
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                if(annotation.annotationType().equals(DBField.class))
                {
                    try {
                        DBField dbField = (DBField)annotation;
                        switch(dbField.type())
                        {
                            case DBField.STRING : field.set(entity,rs.getString(dbField.name()));break;
                            case DBField.INT : field.set(entity,rs.getInt(dbField.name()));break;
                            case DBField.DOUBLE : field.set(entity,rs.getDouble(dbField.name()));break;
                            case DBField.FLOAT : field.set(entity,rs.getFloat(dbField.name()));break;
                            case DBField.BOOLEAN : field.set(entity,rs.getBoolean(dbField.name()));break;
                            case DBField.DATE : field.set(entity,rs.getDate(dbField.name()));break;
                        }
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                }
            }
        }
    }

    protected String getTableName(Class<? extends DBEntity> type)
    {
        for (Annotation annotation : type.getDeclaredAnnotations()) {
            if(annotation.annotationType().equals(DBTable.class))
            {
                return ((DBTable)annotation).name();
            }
        }
        return null;
    }
    protected <T extends DBEntity> Map<DBField,Object> matchAttrAndData(T entity, Class<T> type)
    {
        Map<DBField,Object> params = new HashMap<>();
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Annotation[] annotations = field.getAnnotations();
            System.out.println(field.getName());
            for (Annotation annotation : annotations) {
                if(annotation.annotationType().equals(DBField.class))
                {
                    try {
                        params.putIfAbsent((DBField) annotation,field.get(entity));
                        break;
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        Logger.getLogger(SQLServerDBContext.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
        }
        return params;
    }
}
