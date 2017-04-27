package com.iam360.dscvr.views.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.iam360.dscvr.R;
import com.iam360.dscvr.bus.BusProvider;
import com.iam360.dscvr.bus.RecordFinishedEvent;
import com.iam360.dscvr.bus.RecordFinishedPreviewEvent;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.record.GlobalState;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.CameraUtils;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.util.DBHelper;
import com.iam360.dscvr.util.MixpanelHelper;
import com.squareup.otto.Subscribe;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by Mariel on 4/13/2016.
 */
public class OptoImagePreviewActivity extends AppCompatActivity implements View.OnClickListener {

    @Bind(R.id.statusbar) RelativeLayout statusbar;
    @Bind(R.id.exit_button) Button exitButton;
    @Bind(R.id.description_box) EditText descBox;
    @Bind(R.id.upload_progress) RelativeLayout uploadProgress;
    @Bind(R.id.black_circle) Button blackCircle;
    @Bind(R.id.upload_button) Button uploadButton;
    @Bind(R.id.preview_image) KenBurnsView previewImage;

    private Optograph optographGlobal;
    private String optographId;

    private DBHelper mydb;
    private Cache cache;
    private Context context;

    public final static String optoType360_1 = "optograph_1";
    public final static String optoType360_3 = "optograph_3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_optoimage_preview);
        cache = Cache.open();
        context = this;

        // get bundles
        optographId = getIntent().getStringExtra("id");
        mydb = new DBHelper(this);

        Optograph optograph = new Optograph(optographId);
        optographGlobal = optograph;

        ButterKnife.bind(this, this);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Timber.v("kitkat");
            statusbar.setVisibility(View.VISIBLE);
        } else {
            statusbar.setVisibility(View.GONE);
        }

        uploadButton.setOnClickListener(this);
        exitButton.setOnClickListener(this);

        optograph.setOptograph_type(cache.getInt(Cache.CAMERA_MODE) ==(Constants.ONE_RING_MODE)?optoType360_1:optoType360_3);
        MixpanelHelper.trackCreateOptographPost(this);

    }

    private boolean createDefaultOptograph(Optograph opto) {
        Cursor res = mydb.getData(opto.getId(), DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
        res.moveToFirst();
        boolean ret = false;
        if (res.getCount() == 0) {
            ret = true;
            mydb.insertOptograph(opto.getId(), "", cache.getString(Cache.USER_ID), "", opto.getCreated_atRFC3339(),
                    opto.getDeleted_at(), false, 0, false, false, opto.getStitcher_version(), true, false, "", true, true, false, opto.isPostFacebook(), opto.isPostTwitter(), false,
                    false, false, "", opto.getOptograph_type(), "");
        }
        return ret;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.upload_button:
                finish();
                break;
            case R.id.exit_button:
                exitDialog();
                break;
            case R.id.retry_button:
                exitDialog();
                break;
            default:
                break;

        }

    }

    private void deleteOptographFromPhone(String id) {
        File dir = new File(CameraUtils.PERSISTENT_STORAGE_PATH + id);

        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory()) {
                    for (File file1 : file.listFiles()) {
                        file1.delete();
                    }
                    file.delete();
                }
            }
            dir.delete();
        }
    }

    private void enableButtons() {
        uploadProgress.setVisibility(View.GONE);
        blackCircle.setVisibility(View.GONE);
    }

    private void exitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.dialog_exit_from_preview)
                .setPositiveButton(getResources().getString(R.string.dialog_discard), (dialog, which) -> {
                        deleteOptographFromPhone(optographId);
                        finish();
                }).setNegativeButton(getResources().getString(R.string.dialog_keep), (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }

    @Subscribe
    public void receivePreviewImage(RecordFinishedPreviewEvent recordFinishedPreviewEvent) {
        Timber.d("receivePreviewImage");

        //https://github.com/flavioarfaria/KenBurnsView
        previewImage.setImageBitmap(recordFinishedPreviewEvent.getPreviewImage());

        createDefaultOptograph(optographGlobal);
        enableButtons();

    }

    @Subscribe
    public void receiveFinishedImage(RecordFinishedEvent recordFinishedEvent) {
        enableButtons();
    }

    public void onBackPressed() {
        exitDialog();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!GlobalState.isAnyJobRunning) {
            enableButtons();
        }

        // Register for preview image generation event
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Unregister for preview image generation event
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}
