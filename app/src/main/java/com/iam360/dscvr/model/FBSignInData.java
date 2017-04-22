package com.iam360.dscvr.model;

public class FBSignInData {
    final String facebook_user_id;
    final String facebook_token;

    public FBSignInData(String facebook_user_id, String facebook_token) {
        this.facebook_user_id = facebook_user_id;
        this.facebook_token = facebook_token;
    }
}