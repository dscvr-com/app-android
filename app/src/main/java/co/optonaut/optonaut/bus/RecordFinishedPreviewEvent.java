package co.optonaut.optonaut.bus;

import android.graphics.Bitmap;

/**
 * Created by Mariel on 4/26/2016.
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
