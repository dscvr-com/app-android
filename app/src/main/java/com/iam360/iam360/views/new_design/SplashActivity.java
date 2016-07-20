package com.iam360.iam360.views.new_design;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.model.Person;
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
        String username = cache.getString(Cache.USER_NAME);
        Intent intent;


        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getParcelable("person")!=null) {
            Person person = getIntent().getExtras().getParcelable("person");
            intent = new Intent(this, MainActivity.class);
            intent.putExtra("person", person);
            startActivity(intent);
            finish();
            return;
        }else if(getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getParcelable("opto")!=null){
            Optograph optograph = getIntent().getExtras().getParcelable("opto");
            intent = new Intent(this, MainActivity.class);
            intent.putExtra("opto", optograph);
            startActivity(intent);
            finish();
            return;
        }

        if(token.equals("")) intent = new Intent(this, SigninFBActivity.class);
        else if(username.length() == 0 || username.length() > 15) intent = new Intent(this, CreateUsernameActivity.class);
        else intent = new Intent(this, MainActivity.class);

        startActivity(intent);
        finish();
    }
}
