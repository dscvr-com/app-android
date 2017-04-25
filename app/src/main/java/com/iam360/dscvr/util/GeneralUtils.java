package com.iam360.dscvr.util;

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

import timber.log.Timber;


/**
 * FIXME is this class needed?
 */
public class GeneralUtils {

    /**
     *
     * @param context
     * @param textView or Button because buttons are asignabels of TextView
     * @param typeface Typeface.BOLD, Typeface.ITALIC
     */
    public void setFont(Context context, TextView textView, int typeface) {
        Typeface custom_font = Typeface.createFromAsset(context.getResources().getAssets(), "fonts/Avenir_LT_45_Book_0.ttf");
        textView.setTypeface(custom_font, typeface);
    }
}
