package com.iam360.dscvr.views.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.iam360.dscvr.R;
import com.iam360.dscvr.bus.BusProvider;
import com.iam360.dscvr.bus.RecordFinishedEvent;
import com.iam360.dscvr.record.GlobalState;
import com.squareup.otto.Subscribe;

import timber.log.Timber;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        BusProvider.getInstance().register(this);
    }

    @Subscribe
    public void receiveFinishedImage(RecordFinishedEvent recordFinishedEvent) {
        Timber.d("receiveFinishedImage");
        BusProvider.getInstance().unregister(this);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        if(GlobalState.isAnyJobRunning == true){
            receiveFinishedImage(null);
            return;
        }
        BusProvider.getInstance().register(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        BusProvider.getInstance().unregister(this);
        super.onPause();
    }
}
