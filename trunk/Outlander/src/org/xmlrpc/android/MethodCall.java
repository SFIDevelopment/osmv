package org.xmlrpc.android;

import java.util.ArrayList;

public class MethodCall {

    private static final int TOPIC  = 1;
    String                   methodName;
    ArrayList<Object>        params = new ArrayList<Object>();

    public String getMethodName() {
        return methodName;
    }

    void setMethodName(final String methodName) {
        this.methodName = methodName;
    }

    public ArrayList<Object> getParams() {
        return params;
    }

    void setParams(final ArrayList<Object> params) {
        this.params = params;
    }

    public String getTopic() {
        return (String) params.get(MethodCall.TOPIC);
    }
}
