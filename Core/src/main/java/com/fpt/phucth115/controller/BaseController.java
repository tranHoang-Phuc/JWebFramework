package com.fpt.phucth115.controller;

import com.fpt.phucth115.consts.WebConstants;
import com.fpt.phucth115.helper.MVCHelper;
import com.fpt.phucth115.model.BaseModel;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseController extends HttpServlet {
    public HttpServletRequest request;
    public HttpServletResponse response;

    private boolean isPreActionEndedProcess;

    public void baseProcessRequest(HttpServletRequest request, HttpServletResponse response, String method)
            throws InvocationTargetException, IllegalAccessException, ServletException {
        request.setAttribute("isValidCall", new Object());

        this.request = request;
        this.response = response;

        String currentAction = getAction();

        Method[] methods = this.getClass().getMethods();
        for (Method met : methods) {
            Annotation[] annotations = met.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().getName().equals(GET.class) && method.equals("GET")) {
                    if(((GET) annotation).action().equals(currentAction)) {
                        boolean isEnded = checkPreProcessingAction(met);
                        if (!isEnded) {
                            met.setAccessible(true);
                            Object[] params = requestMapping(met);
                            met.invoke(this, params);
                        }
                        return;
                    }
                    
                }
            }
        }
    }

    private Object[] requestMapping(Method met) throws ServletException {
        List<Object> params = new ArrayList<>();

        List<String> paramNames = new ArrayList<>();
        Annotation[][] annotations = met.getParameterAnnotations();
        for (Annotation[] ann : annotations) {
            if (ann.length > 0) {
                Annotation realAnn = ann[0];
                if (realAnn.annotationType().equals(RequestParam.class)) {
                    paramNames.add(((RequestParam) realAnn).attr());
                }
            }
        }

        Parameter[] parameters = met.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            String value = request.getParameter(paramNames.get(i));
            params.add(convert(value, p.getType()));
        }
        return params.toArray();
    }
    private <O> O convert(Object input, Class<O> otype) {
        return (O) input;
    }
    private boolean checkPreProcessingAction(Method met) {
        try {
            Annotation[] annotations = met.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(PreProcessing.class)) {
                    String methodAttr = ((PreProcessing) annotation).method();
                    Method[] methods = this.getClass().getMethods();
                    for (Method involkedMethod : methods) {
                        if (involkedMethod.getName().equals(methodAttr)) {
                            involkedMethod.setAccessible(true);
                            involkedMethod.invoke(this);
                            if (isPreActionEndedProcess) {
                                return true;
                            }
                        }
                    }
                }
            }

        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(BaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public String getAction() {
        String[] parts = request.getRequestURI().split("/");
        return parts[parts.length - 1].split("[?]")[0];
    }

    public String getController() {
        String[] parts = request.getRequestURI().split("/");
        return parts[parts.length - 2];
    }

    public void forwardToView(BaseModel model) {
        String controller = getController();
        String action = getAction();
        if (controller.isEmpty()) {
            return;
        }
        forwardToView(controller, action, model);
    }

    public void forwardToView(String controller, String action, BaseModel model) {
        try {
            MVCHelper.setModel(request, model);
            request.getRequestDispatcher("../view/" + controller + "/" + action + ".jsp").forward(request, response);
        } catch (ServletException | IOException exception) {
            Logger.getLogger(BaseController.class.getName()).log(Level.SEVERE, null, exception);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            baseProcessRequest(request, response, WebConstants.REQUEST_METHOD_GET);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(BaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            baseProcessRequest(request, response, WebConstants.REQUEST_METHOD_POST);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(BaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
