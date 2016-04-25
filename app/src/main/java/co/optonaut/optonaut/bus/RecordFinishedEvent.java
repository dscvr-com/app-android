package co.optonaut.optonaut.bus;

import android.graphics.Bitmap;

import co.optonaut.optonaut.model.Person;

/**
 * @author Nilan Marktanner
 * @date 2016-02-13
 */
public class RecordFinishedEvent {
    Bitmap previewImage;

    public RecordFinishedEvent(Bitmap previewImage) {
        this.previewImage = previewImage;
    }

    public Bitmap getPreviewImage() {
        return previewImage;
    }

}
