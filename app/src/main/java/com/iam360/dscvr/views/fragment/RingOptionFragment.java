package com.iam360.dscvr.views.fragment;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.iam360.dscvr.R;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.Constants;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * TODO: add Bluetooth
 * <p>
 * Fragment to decide which Mode should be used.
 * Created by Lotti on 4/25/2017.
 */

public class RingOptionFragment extends Fragment {
    private String TAG = RingOptionFragment.class.getSimpleName();
    @Bind(R.id.frag_manual_button)
    ImageButton manualBtn;
    @Bind(R.id.frag_motor_button)
    ImageButton motorBtn;
    @Bind(R.id.frag_option_record_button)
    ImageButton recordButton;

    private Cache cache;

    private OnModeFinished callBackListener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_ring_option, container, false);
        cache = Cache.open();
        ButterKnife.bind(this, view);
        initButtonListeners();
        return view;
    }

    private void initButtonListeners() {
        updateMode((cache.getInt(Cache.CAMERA_MODE) == Constants.ONE_RING_MODE) ? true : false);
        manualBtn.setOnClickListener(v -> updateMode(true));
        motorBtn.setOnClickListener(v -> updateMode(false));
        recordButton.setOnClickListener(v -> finishSettingMode());

    }

    private void finishSettingMode() {
        callBackListener.finishSettingModeForRecording();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callBackListener = (OnModeFinished) context;

    }

    /**
     * Updates 1 or 3 ring on layout and cache
     *
     * @param isManualMode
     */
    private void updateMode(boolean isManualMode) {
        if (isManualMode) {
            manualBtn.setBackgroundResource(R.drawable.manual_icon_orange);
            motorBtn.setBackgroundResource(R.drawable.motor_icon);
            cache.save(Cache.CAMERA_MODE, Constants.ONE_RING_MODE);
        } else {
            manualBtn.setBackgroundResource(R.drawable.manual_icon);
            motorBtn.setBackgroundResource(R.drawable.motor_icon_orange);
            cache.save(Cache.CAMERA_MODE, Constants.THREE_RING_MODE);
        }
    }

    public interface OnModeFinished {
        public void finishSettingModeForRecording();
    }
}
