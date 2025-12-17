package com.fpt.phucth115.helper;

import com.fpt.phucth115.model.BaseModel;
import jakarta.servlet.http.HttpServletRequest;

import static com.fpt.phucth115.consts.WebConstants.GET_MODEL_ATTRIBUTE;

public class MVCHelper {

    public static String getControllerPathFromView(HttpServletRequest request, String controller, String action)
    {
        return "../"+controller+"/"+action;
    }

    public static <T extends BaseModel> T getModel(HttpServletRequest request, Class<T> type)
    {
        return (T)request.getAttribute(GET_MODEL_ATTRIBUTE);
    }

    public static <T extends BaseModel> void setModel(HttpServletRequest request,T model)
    {
        request.setAttribute(GET_MODEL_ATTRIBUTE,model);
    }

    public static <T> T getViewBag(HttpServletRequest request,String key,Class<T> type)
    {
        return (T)request.getAttribute(key);
    }

    public static <T> void setViewBag(HttpServletRequest request,String key,T model)
    {
        request.setAttribute(key,model);
    }
}
