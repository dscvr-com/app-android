package co.optonaut.optonaut.model;

import java.util.UUID;

/**
 * @author Nilan Marktanner
 * @date 2015-11-13
 */

public class Optograph {
    private UUID id;
    private UUID left_texture_asset_id;
    private UUID preview_asset_id;
    private String text;
    private UUID right_texture_asset_id;


    public UUID getId() {
        return id;
    }

    public UUID getLeft_texture_asset_id() {
        return left_texture_asset_id;
    }

    public UUID getPreview_asset_id() {
        return preview_asset_id;
    }

    public String getText() {
        return text;
    }

    public UUID getRight_texture_asset_id() {
        return right_texture_asset_id;
    }
}
