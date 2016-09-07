package com.iam360.dscvr.model;

/**
 * Created by Mariel on 4/26/2016.
 */
public class OptoData {
    final String id;
    final String stitcher_version;
    final String created_at;
    final String optograph_type;//value must be optograph or theta
    final String optograph_platform;
    final String optograph_model;
    final String optograph_make;

    public OptoData(String id, String stitcher_version, String created_at,String optograph_type,
                    String optograph_platform, String optograph_model, String optograph_make) {
        this.id = id;
        this.stitcher_version = stitcher_version;
        this.created_at = created_at;
        this.optograph_type = optograph_type;
        this.optograph_platform = optograph_platform;
        this.optograph_model = optograph_model;
        this.optograph_make = optograph_make;
    }

    @Override
    public String toString() {
        return "OptoData : id=" + id + " stitcher_version=" + stitcher_version + " created_at=" +
                created_at + " type=" + optograph_type +" platform="+optograph_platform+" model="+
                optograph_model+" make="+optograph_make;
    }
}
