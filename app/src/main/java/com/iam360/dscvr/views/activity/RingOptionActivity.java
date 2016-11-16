package com.iam360.dscvr.views.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.iam360.dscvr.R;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.util.GeneralUtils;
import com.iam360.dscvr.views.record.RecorderPreviewView;

public class RingOptionActivity extends AppCompatActivity implements View.OnClickListener {
    private RecorderPreviewView recordPreview;
    private TextView manualTxt;
    private TextView motorTxt;
    private ImageButton manualBtn;
    private ImageButton motorBtn;
    private Cache cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ring_option);

        recordPreview = new RecorderPreviewView(this);
        FrameLayout preview = (FrameLayout) findViewById(R.id.record_preview);
        preview.addView(recordPreview);

        manualTxt = (TextView) findViewById(R.id.manual_text);
        motorTxt = (TextView) findViewById(R.id.motor_text);
        manualBtn = (ImageButton) findViewById(R.id.manual_button);
        motorBtn = (ImageButton) findViewById(R.id.motor_button);

        GeneralUtils generalUtils = new GeneralUtils();
        generalUtils.setFont(this, manualTxt, Typeface.BOLD);
        generalUtils.setFont(this, motorTxt, Typeface.BOLD);

        cache = Cache.open();

        int mode = cache.getInt(Cache.CAMERA_MODE);
        updateMode((mode == Constants.ONE_RING_MODE) ? true : false);
    }

    @Override
    public void onResume() {
        super.onResume();
        recordPreview.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        recordPreview.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.manual_button:
                updateMode(true);
                break;
            case R.id.motor_button:
                updateMode(false);
                break;
            case R.id.record_button:
                Intent intent;
                intent = new Intent(RingOptionActivity.this, RecorderActivity.class);
                startActivity(intent);

                finish();
                break;
        }
    }

    private void updateMode(boolean isManualMode) {
        if(isManualMode) {
            manualBtn.setBackground(getResources().getDrawable(R.drawable.manual_icon_orange));
            motorBtn.setBackground(getResources().getDrawable(R.drawable.motor_icon));
            cache.save(Cache.CAMERA_MODE, Constants.ONE_RING_MODE);
        } else {
            manualBtn.setBackground(getResources().getDrawable(R.drawable.manual_icon));
            motorBtn.setBackground(getResources().getDrawable(R.drawable.motor_icon_orange));
            cache.save(Cache.CAMERA_MODE, Constants.THREE_RING_MODE);
        }
    }

}
