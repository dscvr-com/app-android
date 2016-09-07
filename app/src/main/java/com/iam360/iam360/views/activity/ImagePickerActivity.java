package com.iam360.iam360.views.activity;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.iam360.iam360.R;
import com.iam360.iam360.util.GeneralUtils;
import com.iam360.iam360.viewmodels.ImagePickerAdapter;

import java.util.ArrayList;

public class ImagePickerActivity extends AppCompatActivity {

    public static final int MIN_WIDTH = 5000;
    public static final int NUM_COLUMNS_OPTO = 3;
    public static final int NUM_COLUMNS_STORY = 4;

    public static final int UPLOAD_OPTO_MODE = 0;
    public static final int CREATE_STORY_MODE = 1;
    public static final int ADD_SCENE_MODE = 2;

    public static final String PICKER_MODE = "mode";

    private int MODE = UPLOAD_OPTO_MODE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        MODE = getIntent().getExtras().getInt(PICKER_MODE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView title = (TextView) findViewById(R.id.title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        if(MODE == UPLOAD_OPTO_MODE) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.optonautMain_2));
            title.setText(getResources().getString(R.string.image_picker_your_images));
            title.setTextColor(Color.WHITE);
        }

        new GeneralUtils().setFont(this, title, Typeface.BOLD);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        GridLayoutManager layoutManager = new GridLayoutManager(this, MODE == UPLOAD_OPTO_MODE ? NUM_COLUMNS_OPTO : NUM_COLUMNS_STORY);
        recyclerView.setLayoutManager(layoutManager);

        ImagePickerAdapter adapter = new ImagePickerAdapter(this, listImages(), MODE);
        recyclerView.setAdapter(adapter);
    }

    private ArrayList<String> listImages() {
        //where contextObject is your activity
        ContentResolver cr = getContentResolver();

        ArrayList<String> imagePathList = new ArrayList<String>();
        String[] columns = new String[] {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.TITLE,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.MIME_TYPE,
                MediaStore.Images.ImageColumns.WIDTH,
                MediaStore.Images.ImageColumns.HEIGHT,
                MediaStore.Images.ImageColumns.SIZE,
                MediaStore.Images.ImageColumns.DATE_TAKEN };

        Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        if (cursor.moveToFirst()) {
            do {
//                final String title = cursor.getString(cursor.getColumnIndex(columns[1]));
                final String data = cursor.getString(cursor.getColumnIndex(columns[2]));
//                final String mimeType = cursor.getString(cursor.getColumnIndex(columns[3]));
                final String width = cursor.getString(cursor.getColumnIndex(columns[4]));
//                final String height = cursor.getString(cursor.getColumnIndex(columns[5]));
//                final String size = cursor.getString(cursor.getColumnIndex(columns[6]));
//                final String date = cursor.getString(cursor.getColumnIndex(columns[7]));
//                Timber.d("PATH : " + title + ":" + data + ":" + mimeType + ":" + width + ":" + height + ":" + size);

                if(width != null && Integer.parseInt(width) > MIN_WIDTH) {
                    imagePathList.add(data);
//                    Timber.d("360IMAGES " + date);
                }

            } while (cursor.moveToNext());
        }
        cursor.close();

        return imagePathList;

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
}
