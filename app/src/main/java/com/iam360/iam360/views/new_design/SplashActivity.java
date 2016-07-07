package com.iam360.iam360.views.new_design;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.iam360.iam360.util.Cache;
import com.iam360.iam360.views.profile.SigninFBActivity;

public class SplashActivity extends AppCompatActivity {

    private Cache cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // instatiate cache on start of application
        cache = Cache.getInstance(this);
        cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);
        Intent intent;

//        if(token.equals("")) intent = new Intent(this, SigninFBActivity.class);
//        else intent = new Intent(this, MainActivity.class);

        intent = new Intent(this, SigninFBActivity.class);

        startActivity(intent);
        finish();
    }
}
