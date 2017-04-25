package com.iam360.dscvr.views.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.iam360.dscvr.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * TODO: add Bluetooth
 *
 * Fragment to decide which Mode should be used.
 * Created by Lotti on 4/25/2017.
 */

public class RingOptionFragment extends Fragment {
    private String TAG = RingOptionFragment.class.getSimpleName();
    @Bind(R.id.frag_manual_text) private TextView manualTxt;
    @Bind(R.id.frag_motor_text) private TextView motorTxt;
    @Bind(R.id.frag_manual_button) private ImageButton manualBtn;
    @Bind(R.id.frag_motor_button) private ImageButton motorBtn;

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
        ButterKnife.bind(this, view);
        return view;
    }
}
