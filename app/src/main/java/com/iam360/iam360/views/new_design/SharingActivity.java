package com.iam360.iam360.views.new_design;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.iam360.iam360.R;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.views.profile.ProfileFragmentExercise;

public class SharingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.animator.from_bottom_to_top, R.animator.from_top_to_bottom);
        setContentView(R.layout.activity_share);
        Optograph optograph = getIntent().getExtras().getParcelable("opto");

        SharingFragment sharingFragment = SharingFragment.newInstance(optograph);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, sharingFragment).commit();
//        sharingFragment.updateOptograph();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        overridePendingTransition(R.animator.from_bottom_to_top, R.animator.from_top_to_bottom);
    }
}
