package co.optonaut.optonaut.views;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.util.Constants;

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
        textView.setText("Vanilla");
    }

}