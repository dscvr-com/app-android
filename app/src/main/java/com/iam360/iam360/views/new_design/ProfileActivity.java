package com.iam360.iam360.views.new_design;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.iam360.iam360.R;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.views.profile.OptographLocalGridAdapter;
import com.iam360.iam360.views.profile.ProfileFragment;

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
