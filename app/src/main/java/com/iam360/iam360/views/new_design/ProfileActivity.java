package com.iam360.iam360.views.new_design;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.iam360.iam360.R;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.views.profile.ProfileFragment;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Person person = getIntent().getExtras().getParcelable("person");

        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, ProfileFragment.newInstance(person)).commit();

    }

}
