package co.optonaut.optonaut.views.new_design;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Person;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.views.profile.ProfileFragment;

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
