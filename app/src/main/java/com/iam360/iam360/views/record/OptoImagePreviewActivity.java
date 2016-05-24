package com.iam360.iam360.views.record;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.iam360.iam360.R;
import com.iam360.iam360.views.profile.SignInActivity;

/**
 * Created by Mariel on 4/13/2016.
 */
public class OptoImagePreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    private Dialog retryDialog() {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_retry_recording)
                .setPositiveButton(getResources().getString(R.string.dialog_discard), (dialog, which) -> {
                    //how can i call an Activity here???
                    Intent intent= new Intent(this, SignInActivity.class);
//                    intent.putextra("your_extra","your_class_value");
                    this.startActivity(intent);
                }).setNegativeButton(getResources().getString(R.string.dialog_keep), (dialog, which) -> {
            dialog.dismiss();
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
