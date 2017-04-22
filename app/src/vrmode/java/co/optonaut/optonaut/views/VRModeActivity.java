package com.iam360.dscvr.views;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.iam360.dscvr.R;

/**
 * @author Nilan Marktanner
 * @date 2015-12-30
 */

public class VRModeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vrmode);

        TextView textView = (TextView) findViewById(R.id.textview);
        textView.setText("VRMode");
    }

}