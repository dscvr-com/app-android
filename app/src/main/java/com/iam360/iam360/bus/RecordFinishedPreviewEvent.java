package com.iam360.iam360.bus;

import android.graphics.Bitmap;

/**
 * @author Nilan Marktanner
 * @date 2016-02-13
 */
public class RecordFinishedPreviewEvent {
    Bitmap previewImage;

    public RecordFinishedPreviewEvent(Bitmap previewImage) {
        this.previewImage = previewImage;
    }

    public Bitmap getPreviewImage() {
        return previewImage;
    }

}
