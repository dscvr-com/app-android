package co.optonaut.optonaut.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import timber.log.Timber;

public class GeneralUtils {

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
}
