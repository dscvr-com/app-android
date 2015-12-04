package co.optonaut.optonaut.views;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.model.Person;

/**
 * @author Nilan Marktanner
 * @date 2015-11-13
 */
public class MainActivity extends AppCompatActivity {
    private final String FEED_FRAGMENT_TAG = "FEED_FRAGMENT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (findViewById(R.id.fragment_placeholder) != null) {
            if (savedInstanceState != null) {
                return;
            }
            FeedFragment feedFragment = new FeedFragment();

            feedFragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_placeholder, new FeedFragment(), FEED_FRAGMENT_TAG).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openOptograph2DView(Optograph optograph) {
        Optograph2DFragment optograph2DFragment = new Optograph2DFragment();
        Bundle args = new Bundle();
        args.putParcelable("optograph", optograph);
        optograph2DFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction().
                replace(R.id.fragment_placeholder, optograph2DFragment).addToBackStack(null).commit();
    }

    public void openProfileFragment(Person person) {
        ProfileFragment profileFragment= new ProfileFragment();
        Bundle args = new Bundle();
        args.putParcelable("person", person);
        profileFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction().
                replace(R.id.fragment_placeholder, profileFragment).addToBackStack(null).commit();
    }


}
