package co.optonaut.optonaut.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import org.joda.time.DateTime;

import co.optonaut.optonaut.util.RFC3339DateFormatter;

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

    private String created_at;

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

    public String getCreated_at() {
        return created_at;
    }

    public DateTime getCreated_atDateTime() {
        return RFC3339DateFormatter.fromRFC3339String(getCreated_at());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Optograph optograph = (Optograph) o;

        return uuid.equals(optograph.uuid);
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (left_texture_asset_id != null ? left_texture_asset_id.hashCode() : 0);
        result = 31 * result + (preview_asset_id != null ? preview_asset_id.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (right_texture_asset_id != null ? right_texture_asset_id.hashCode() : 0);
        result = 31 * result + (created_at != null ? created_at.hashCode() : 0);
        return result;
    }
}
