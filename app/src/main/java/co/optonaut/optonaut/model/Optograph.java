package co.optonaut.optonaut.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

/**
 * @author Nilan Marktanner
 * @date 2015-11-13
 */

public class Optograph {
    private String uuid;
    private String left_texture_asset_id;
    private String preview_asset_id;
    private String text;
    private String right_texture_asset_id;

    public String getUuid() {
        return uuid;
    }

    public String getLeft_texture_asset_id() {
        return left_texture_asset_id;
    }

    public String getPreview_asset_id() {
        return preview_asset_id;
    }

    public String getText() {
        return text;
    }

    public String getRight_texture_asset_id() {
        return right_texture_asset_id;
    }
}
