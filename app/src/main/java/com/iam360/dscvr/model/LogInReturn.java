package com.iam360.dscvr.model;

/**
 * Created by Mariel on 3/30/2016.
 */
/*
{
"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjgzNWYwOGJlLTFjZTAtNDhiZC1iODlhLWM2OTM3NDc4YWI5NiJ9.N7bl7pzJDwX4Xeo1fuys5eK70mHAReLGvx2l-ZWGqRI",
"id": "835f08be-1ce0-48bd-b89a-c6937478ab96",
"onboarding_version": 0
}
*/
public class LogInReturn {

    public class EmptyResponse {
        EmptyResponse() {}
    }

    private String token;
    private String id;
    private int onboarding_version;
    private String message;

    public LogInReturn() {
        token = "";
        id = "";
        onboarding_version = 0;
        message = "";
    }

    public String getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public int getOnBoardingVersion() {
        return onboarding_version;
    }

    public String getMessage() {return message;}
}
