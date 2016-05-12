package co.optonaut.optonaut.views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.util.Cache;
import co.optonaut.optonaut.util.Constants;

/**
 * Created by Mariel on 5/11/2016.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener {

    private ImageButton oneRingButton;
    private ImageButton threeRingButton;
    private ImageButton manualButton;
    private ImageButton motorButton;
    private TextView oneRingText;
    private TextView threeRingText;
    private TextView manualText;
    private TextView motorText;

    private Cache cache;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        cache = Cache.open();

        oneRingButton = (ImageButton) view.findViewById(R.id.one_ring_button);
        threeRingButton = (ImageButton) view.findViewById(R.id.three_ring_button);
        manualButton = (ImageButton) view.findViewById(R.id.manual_button);
        motorButton = (ImageButton) view.findViewById(R.id.motor_button);
        oneRingText = (TextView) view.findViewById(R.id.one_ring_text);
        threeRingText = (TextView) view.findViewById(R.id.three_ring_text);
        manualText = (TextView) view.findViewById(R.id.manual_text);
        motorText = (TextView) view.findViewById(R.id.motor_text);


        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.one_ring_button:
                if (cache.getInt(Cache.CAMERA_MODE)!=Constants.ONE_RING_MODE) {
                    cache.save(Cache.CAMERA_MODE, Constants.ONE_RING_MODE);
                    oneRingButton.setBackgroundResource(R.drawable.one_ring_active_icn);
                    oneRingText.setTextColor(getResources().getColor(R.color.text_active));
                    threeRingButton.setBackgroundResource(R.drawable.three_ring_inactive_icn);
                    threeRingText.setTextColor(getResources().getColor(R.color.text_inactive));
                }
                break;
            case R.id.three_ring_button:
                if (cache.getInt(Cache.CAMERA_MODE)!=Constants.THREE_RING_MODE) {
                    cache.save(Cache.CAMERA_MODE, Constants.THREE_RING_MODE);
                    threeRingButton.setBackgroundResource(R.drawable.three_ring_active_icn);
                    threeRingText.setTextColor(getResources().getColor(R.color.text_active));
                    oneRingButton.setBackgroundResource(R.drawable.one_ring_inactive_icn);
                    oneRingText.setTextColor(getResources().getColor(R.color.text_inactive));
                }
                break;
            case R.id.manual_button:
                if (cache.getInt(Cache.CAMERA_CAPTURE_TYPE)!=Constants.MANUAL_MODE) {
                    cache.save(Cache.CAMERA_CAPTURE_TYPE,Constants.MANUAL_MODE);
                    manualButton.setBackgroundResource(R.drawable.manual_active_icn);
                    manualText.setTextColor(getResources().getColor(R.color.text_active));
                    motorButton.setBackgroundResource(R.drawable.motor_inactive_icn);
                    motorText.setTextColor(getResources().getColor(R.color.text_inactive));
                }
                break;
            case R.id.motor_button:
                if (cache.getInt(Cache.CAMERA_CAPTURE_TYPE)!=Constants.MOTOR_MODE) {
                    cache.save(Cache.CAMERA_CAPTURE_TYPE,Constants.MOTOR_MODE);
                    motorButton.setBackgroundResource(R.drawable.motor_active_icn);
                    motorText.setTextColor(getResources().getColor(R.color.text_active));
                    manualButton.setBackgroundResource(R.drawable.manual_inactive_icn);
                    manualText.setTextColor(getResources().getColor(R.color.text_inactive));
                }
                break;
            default:
                break;
        }
    }
}
