package com.iam360.iam360.views.new_design;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.iam360.iam360.R;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.views.profile.ProfileFragment;
import com.iam360.iam360.views.profile.ProfileFragmentExercise;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.animator.from_right_to_left, R.animator.from_left_to_right);
        setContentView(R.layout.activity_profile);
        if (getIntent().getExtras().getParcelable("person")!=null) {
            Person person = getIntent().getExtras().getParcelable("person");
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, ProfileFragmentExercise.newInstance(person)).commit();
        } else {
            String id = getIntent().getStringExtra("id");
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, ProfileFragmentExercise.newInstance(id)).commit();
        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.animator.to_right, R.animator.to_left);
    }
}
