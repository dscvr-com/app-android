package com.iam360.dscvr.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Nilan Marktanner
 * @date 2016-01-30
 */

/*
{
    "name": "Crown Regency Hotel",
    "country": "Philippines",
    "country_short": "PH",
    "place": "Makati",
    "region": "Metro Manila",
    "POI": true
}
 */
public class GeocodeDetails implements Parcelable {
    private String name;
    private String country;
    private String country_short;
    private String place;
    private String region;
    private boolean POI;

    public GeocodeDetails() {
        name = "";
        country = "";
        country_short = "";
        place = "";
        region = "";
        POI = false;
    }
    public GeocodeDetails(Parcel source) {
        // SAME ORDER AS IN writeToParcel!
        this.name = source.readString();
        this.country = source.readString();
        this.country_short = source.readString();
        this.place = source.readString();
        this.region = source.readString();
        this.POI = source.readByte() != 0 ;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getCountry_short() {
        return country_short;
    }

    public String getPlace() {
        return place;
    }

    public String getRegion() {
        return region;
    }

    public boolean isPoi() {
        return POI;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // SAME ORDER AS IN Location(Parcel source)!
        dest.writeString(this.name);
        dest.writeString(this.country);
        dest.writeString(this.country_short);
        dest.writeString(this.place);
        dest.writeString(this.region);
        dest.writeByte((byte) (this.POI ? 1 : 0));
    }

    public static final Creator<GeocodeDetails> CREATOR =
            new Creator<GeocodeDetails>() {

                @Override
                public GeocodeDetails createFromParcel(Parcel source) {
                    return new GeocodeDetails(source);
                }

                @Override
                public GeocodeDetails[] newArray(int size) {
                    return new GeocodeDetails[size];
                }
            };
}
