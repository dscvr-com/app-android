package co.optonaut.optonaut.model;

import com.squareup.okhttp.RequestBody;

/**
 * Created by Mariel on 4/26/2016.
 */
public class OptoImageData {
    final String key;
    final RequestBody asset;
    OptoImageData(String key,RequestBody asset) {
        this.key = key;
        this.asset = asset;
    }
}
