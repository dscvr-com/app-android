package com.iam360.dscvr.model;

/**
 * Created by Joven on 10/18/2016.
 */
public class MapiResponseObject {
    private String data;
    private String status;
    private String message;

    public MapiResponseObject(){
        data = "";
        status = "";
        message = "";
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    private class MapiResponseObjectData {
    }
}
