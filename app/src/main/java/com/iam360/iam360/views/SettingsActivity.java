package com.iam360.iam360.views;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.iam360.iam360.R;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.Constants;

/**
 * Created by Mariel on 5/11/2016.
 */
public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton oneRingButton;
    private ImageButton threeRingButton;
    private ImageButton manualButton;
    private ImageButton motorButton;
    private TextView oneRingText;
    private TextView threeRingText;
    private TextView manualText;
    private TextView motorText;

    private Cache cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        cache = Cache.open();

        oneRingButton = (ImageButton) this.findViewById(R.id.one_ring_button);
        threeRingButton = (ImageButton) this.findViewById(R.id.three_ring_button);
        manualButton = (ImageButton) this.findViewById(R.id.manual_button);
        motorButton = (ImageButton) this.findViewById(R.id.motor_button);
        oneRingText = (TextView) this.findViewById(R.id.one_ring_text);
        threeRingText = (TextView) this.findViewById(R.id.three_ring_text);
        manualText = (TextView) this.findViewById(R.id.manual_text);
        motorText = (TextView) this.findViewById(R.id.motor_text);

        oneRingButton.setOnClickListener(this);
        threeRingButton.setOnClickListener(this);
        manualButton.setOnClickListener(this);
        motorButton.setOnClickListener(this);

        initializeButtons();
    }

    private void initializeButtons() {
        if (cache.getInt(Cache.CAMERA_MODE)==Constants.ONE_RING_MODE)
            activeOneRing();
        else activeThreeRing();

        if (cache.getInt(Cache.CAMERA_CAPTURE_TYPE)==Constants.MANUAL_MODE)
            activeManualType();
        else activeMotorType();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.one_ring_button:
                if (cache.getInt(Cache.CAMERA_MODE)!=Constants.ONE_RING_MODE) {
                    cache.save(Cache.CAMERA_MODE, Constants.ONE_RING_MODE);
                    activeOneRing();
                }
                break;
            case R.id.three_ring_button:
                if (cache.getInt(Cache.CAMERA_MODE)!=Constants.THREE_RING_MODE) {
                    cache.save(Cache.CAMERA_MODE, Constants.THREE_RING_MODE);
                    activeThreeRing();
                }
                break;
            case R.id.manual_button:
                if (cache.getInt(Cache.CAMERA_CAPTURE_TYPE)!=Constants.MANUAL_MODE) {
                    cache.save(Cache.CAMERA_CAPTURE_TYPE, Constants.MANUAL_MODE);
                    activeManualType();
                }
                break;
            case R.id.motor_button:
                if (cache.getInt(Cache.CAMERA_CAPTURE_TYPE)!=Constants.MOTOR_MODE) {
                    cache.save(Cache.CAMERA_CAPTURE_TYPE, Constants.MOTOR_MODE);
                    activeMotorType();
                }
                break;
            default:
                break;
        }
    }

    private void activeOneRing() {
        oneRingButton.setBackgroundResource(R.drawable.one_ring_active_icn);
        oneRingText.setTextColor(getResources().getColor(R.color.text_active));
        threeRingButton.setBackgroundResource(R.drawable.three_ring_inactive_icn);
        threeRingText.setTextColor(getResources().getColor(R.color.text_inactive));
    }

    private void activeThreeRing() {
        threeRingButton.setBackgroundResource(R.drawable.three_ring_active_icn);
        threeRingText.setTextColor(getResources().getColor(R.color.text_active));
        oneRingButton.setBackgroundResource(R.drawable.one_ring_inactive_icn);
        oneRingText.setTextColor(getResources().getColor(R.color.text_inactive));
    }

    private void activeManualType() {
        manualButton.setBackgroundResource(R.drawable.manual_active_icn);
        manualText.setTextColor(getResources().getColor(R.color.text_active));
        motorButton.setBackgroundResource(R.drawable.motor_inactive_icn);
        motorText.setTextColor(getResources().getColor(R.color.text_inactive));
    }

    private void activeMotorType() {
        motorButton.setBackgroundResource(R.drawable.motor_active_icn);
        motorText.setTextColor(getResources().getColor(R.color.text_active));
        manualButton.setBackgroundResource(R.drawable.manual_inactive_icn);
        manualText.setTextColor(getResources().getColor(R.color.text_inactive));
    }

}
