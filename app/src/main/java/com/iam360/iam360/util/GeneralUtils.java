package com.iam360.iam360.util;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import me.leolin.shortcutbadger.ShortcutBadger;
import timber.log.Timber;

public class GeneralUtils {

    /**
     * For the gallery image picker
     * @param context
     * @param contentUri
     * @return
     */
    public String getRealPathFromURI(Context context, Uri contentUri) {

        // ContentType1 = content://com.android.providers.media.documents/document/image:3951
        // ContentType2 = content://media/external/images/media/3951

        boolean isContentType1 = false;
        String wholeID = "";
        String result = "";

        // Will return "image:x*"
        try {
            wholeID = DocumentsContract.getDocumentId(contentUri);
            isContentType1 = true;
        } catch (IllegalArgumentException e) {
            isContentType1 = false;
        }

        if(isContentType1) {
            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];
            String[] column = {MediaStore.Images.Media.DATA};
            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            Cursor cursor = context.getContentResolver().
                    query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            column, sel, new String[]{id}, null);

            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst()) {
                result = cursor.getString(columnIndex);
            }

            cursor.close();

            Timber.d("getRealPathFromURI " + result);
            return result;

        } else {
            Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
            if (cursor == null) {
                result = contentUri.getPath();
            } else {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
                cursor.close();
            }
            Timber.d("getRealPathFromURI " + result);
            return result;
        }

    }

    /**
     * For activities with translucent layout below the status bar
     * @param context
     * @return
     */
    public int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void setStatusBarTranslucent(Activity activity, boolean makeTranslucent) {
        if (makeTranslucent) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
    public void setFont(Context context, TextView textView) {
        setFont(context, textView, Typeface.NORMAL);
    }

    /**
     *
     * @param context
     * @param textView
     * @param typeface Typeface.BOLD, Typeface.ITALIC
     */
    public void setFont(Context context, TextView textView, int typeface) {
        Typeface custom_font = Typeface.createFromAsset(context.getResources().getAssets(), "fonts/Avenir_LT_45_Book_0.ttf");
        textView.setTypeface(custom_font, typeface);
    }

    /**
     *
     * @param context
     * @param button
     * @param typeface Typeface.BOLD, Typeface.ITALIC
     */
    public void setFont(Context context, Button textView, int typeface) {
        Typeface custom_font = Typeface.createFromAsset(context.getResources().getAssets(), "fonts/Avenir_LT_45_Book_0.ttf");
        textView.setTypeface(custom_font, typeface);
    }

    public static String mToString(float[] m) {
        StringBuilder sb = new StringBuilder();
        int l = (int)(Math.sqrt(m.length) + 0.5);
        assert l * l == m.length;
        for(int i = 0; i < l; i++) {
            for(int j = 0; j < l; j++) {
                sb.append(m[j + i * l]);
                if(j != l - 1)
                    sb.append(", ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public void decrementBadgeCount(Cache cache, Context context) {
        int badgeCount = cache.getInt(Cache.NOTIF_COUNT);
        ShortcutBadger.applyCount(context, --badgeCount);
        cache.save(Cache.NOTIF_COUNT, badgeCount);
    }


}
