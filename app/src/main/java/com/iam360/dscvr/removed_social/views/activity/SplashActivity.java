package com.iam360.dscvr.removed_social.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.GeneralUtils;

public class SplashActivity extends AppCompatActivity {

    private Cache cache;
    private GeneralUtils generalUtils = new GeneralUtils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // instatiate cache on start of application
        cache = Cache.getInstance(this);
        cache = Cache.open();
        Intent intent = new Intent(this, MainActivity.class);

        startActivity(intent);
        finish();
    }
}
