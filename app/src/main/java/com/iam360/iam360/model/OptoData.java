package com.iam360.iam360.model;

/**
 * Created by Mariel on 4/26/2016.
 */
public class OptoData {
    final String id;
    final String stitcher_version;
    final String created_at;
    final String optograph_type;//value must be optograph or theta

    public OptoData(String id, String stitcher_version, String created_at,String optograph_type) {
        this.id = id;
        this.stitcher_version = stitcher_version;
        this.created_at = created_at;
        this.optograph_type = optograph_type;
    }
}
