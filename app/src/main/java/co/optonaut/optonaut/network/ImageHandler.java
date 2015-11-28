package co.optonaut.optonaut.network;

/**
 * @author Nilan Marktanner
 * @date 2015-11-28
 */
public class ImageHandler {
    private static final String BASE_URL = "http://optonaut-ios-beta-staging.s3.amazonaws.com/original/";
    private static final String TYPE = ".jpg";

    public static String buildImageUrl(String preview_asset_id) {
        return String.format("%s%s%s", BASE_URL, preview_asset_id, TYPE);
    }
}
