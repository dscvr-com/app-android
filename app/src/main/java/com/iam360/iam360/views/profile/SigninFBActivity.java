package com.iam360.iam360.views.profile;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.iam360.iam360.R;

public class SigninFBActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin_fb);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, SigninFBFragment.newInstance("", "")).commit();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
