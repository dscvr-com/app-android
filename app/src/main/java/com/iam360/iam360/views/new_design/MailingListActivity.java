package com.iam360.iam360.views.new_design;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.iam360.iam360.R;
import com.iam360.iam360.model.Person;
import com.iam360.iam360.util.GeneralUtils;
import com.iam360.iam360.views.profile.ProfileFragmentExercise;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MailingListActivity extends AppCompatActivity {

    @Bind(R.id.type_text) TextView typeText;
    @Bind(R.id.get_text) TextView getText;
    @Bind(R.id.go_btn) Button goBtn;
    @Bind(R.id.send_btn) Button sendBtn;
    @Bind(R.id.reach_us_text) TextView reachUsText;
    @Bind(R.id.secret_code_text) EditText codeText;
    @Bind(R.id.email_text) EditText emailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.animator.from_right_to_left, R.animator.from_left_to_right);
        setContentView(R.layout.activity_mailing_list);

        ButterKnife.bind(this);

        GeneralUtils utils = new GeneralUtils();
        utils.setFont(this, typeText);
        utils.setFont(this, getText);
        utils.setFont(this, goBtn, Typeface.DEFAULT_BOLD);
        utils.setFont(this, sendBtn, Typeface.DEFAULT_BOLD);
        utils.setFont(this, reachUsText);
        utils.setFont(this, emailText);
        utils.setFont(this, codeText);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.animator.to_right, R.animator.to_left);
    }
}
