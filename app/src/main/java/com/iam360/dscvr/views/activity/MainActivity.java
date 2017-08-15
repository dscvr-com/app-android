package com.iam360.dscvr.views.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.iam360.dscvr.DscvrApp;
import com.iam360.dscvr.R;
import com.iam360.dscvr.sensors.DefaultListeners;
import com.iam360.dscvr.sensors.GestureDetectors;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.util.MixpanelHelper;
import com.iam360.dscvr.views.fragment.MainFeedFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private Cache cache;
    private MainFeedFragment mainFeedFragment;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();

        setContentView(R.layout.a_activity_main);
        getSupportFragmentManager().beginTransaction().add(R.id.container, mainFeedFragment).commit();

        MixpanelHelper.trackAppLaunch(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initializeComponents() {

        if (!Cache.cacheInitialized) {
            cache = Cache.getInstance(this);
        }
        cache = Cache.open();

        Constants.initializeConstants(this);
        GestureDetectors.initialize(this);
        DefaultListeners.initialize(this);

        mainFeedFragment = new MainFeedFragment();
        try {
            DscvrApp.getInstance().getConnector().connect((gatt) -> connected(), () -> upperButton(), () -> lowerButton());
        }catch(IllegalStateException e){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
//        copyAssets();

    }

    private void lowerButton() {

//        cache.save(Cache.MOTOR_ON, true);
//        cache.save(Cache.CAMERA_MODE, Constants.THREE_RING_MODE);
//        Intent i = new Intent(this, RecorderActivity.class);
//        i.putExtra(RecorderActivity.DIRECTLY_START_FROM_REMOTE, true);
//        startActivity(i);
    }

    private void upperButton() {
        cache.save(Cache.MOTOR_ON, true);
        cache.save(Cache.CAMERA_MODE, Constants.ONE_RING_MODE);
        Intent i = new Intent(this, RecorderActivity.class);
        i.putExtra(RecorderActivity.DIRECTLY_START_FROM_REMOTE, true);
        startActivity(i);
    }

    private void connected() {
        runOnUiThread(() -> Toast.makeText(this,"Connected", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                DscvrApp.getInstance().getConnector().connect((gatt) -> connected(), () -> upperButton(), () -> lowerButton());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    //Unregistering receiver on activity paused
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        Timber.d("Workaround for bug 19917 value");
        // https://code.google.com/p/android/issues/detail?id=19917
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    public void switchToVRMode() {
        mainFeedFragment.switchToVRMode();
    }

    private void copyAssets() {
        Timber.d("copyAssets");

        String filename = "logo-text.png";
        InputStream in = null;
        OutputStream out = null;
        try {
            in = getAssets().open(filename);
            File outFile = new File(getExternalFilesDir(null), filename);
            out = new FileOutputStream(outFile);
            copyFile(in, out);
            Timber.d("File : " + outFile.getAbsolutePath());
        } catch(IOException e) {
            Timber.d("ioxception");
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
        }

    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }


}