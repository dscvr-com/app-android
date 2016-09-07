package com.iam360.dscvr.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.iam360.dscvr.R;
import com.iam360.dscvr.model.Person;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.GeneralUtils;
import com.iam360.dscvr.viewmodels.OptographLocalGridAdapter;
import com.iam360.dscvr.views.fragment.ProfileFragmentExercise;

public class ProfileActivity extends AppCompatActivity {

    private Cache cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.animator.from_right_to_left, R.animator.from_left_to_right);
        setContentView(R.layout.activity_profile);

        cache = Cache.open();
        if (getIntent().getExtras().getParcelable("notif")!=null) {
            new GeneralUtils().decrementBadgeCount(cache, this);
        }

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

    public void refresh() {
        for (Fragment frag:getSupportFragmentManager().getFragments()) {
            if (frag instanceof ProfileFragmentExercise) ((ProfileFragmentExercise) frag).refresh();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OptographLocalGridAdapter.DELETE_IMAGE && resultCode == RESULT_OK &&
                data != null) {
            for (Fragment frag : getSupportFragmentManager().getFragments()) {
                if (frag instanceof ProfileFragmentExercise) {
                    String id = data.getStringExtra("id");
                    boolean local = data.getBooleanExtra("local",false);
                    Log.d("myTag", " delete: id: " + id + " local? " + local);
                    ((ProfileFragmentExercise) frag).refreshAfterDelete(id,local);
                }
            }
        }
    }
}
