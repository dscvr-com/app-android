package com.iam360.iam360.views.new_design;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.iam360.iam360.R;

import java.util.ArrayList;

public class ImagePicker extends AppCompatActivity {

    private final int MIN_WIDTH = 5000;
    private final int NUM_COLUMNS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        GridLayoutManager layoutManager = new GridLayoutManager(this, NUM_COLUMNS);
        recyclerView.setLayoutManager(layoutManager);


        ImagePickerAdapter adapter = new ImagePickerAdapter(this, listImages());
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
                MediaStore.Images.ImageColumns.SIZE };
        Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns, null, null, null);

        if (cursor.moveToFirst()) {
            do {
//                final String title = cursor.getString(cursor.getColumnIndex(columns[1]));
                final String data = cursor.getString(cursor.getColumnIndex(columns[2]));
//                final String mimeType = cursor.getString(cursor.getColumnIndex(columns[3]));
                final String width = cursor.getString(cursor.getColumnIndex(columns[4]));
//                final String height = cursor.getString(cursor.getColumnIndex(columns[5]));
//                final String size = cursor.getString(cursor.getColumnIndex(columns[6]));
//                Timber.d("PATH : " + title + ":" + data + ":" + mimeType + ":" + width + ":" + height + ":" + size);

                if(Integer.parseInt(width) > MIN_WIDTH) imagePathList.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return imagePathList;

    }
}
