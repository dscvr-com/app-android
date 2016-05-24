package com.iam360.iam360.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Nilan Marktanner
 * @date 2016-01-30
 */

/*
location":{
    "id":"f992e007-22be-45ab-932a-86c7d5102185",
    "created_at":"2016-01-16T10:11:16.035Z",
    "updated_at":"0001-01-01T00:00:00Z",
    "deleted_at":null,
    "latitude":45.86098098754883,
    "longitude":7.811460971832275,
    "text":"Chalet du Lys",
    "country":"Italy",
    "country_short":"",
    "place":"",
    "region":"",
    "poi":true
}
 */
public class Location implements Parcelable {
    private String id;
    private String created_at;
    private String updated_at;
    private String deleted_at;
    private String latitude;
    private String longitude;
    private String text;
    private String country;
    private String country_short;
    private String place;
    private String region;
    private boolean poi;

    public Location() {
        id = "";
        created_at = "";
        updated_at = "";
        deleted_at = "";
        latitude = "";
        longitude = "";
        text = "";
        country = "";
        country_short = "";
        place = "";
        region = "";
        poi = false;
    }
    public Location(Parcel source) {
        // SAME ORDER AS IN writeToParcel!
        this.id = source.readString();
        this.created_at = source.readString();
        this.updated_at = source.readString();
        this.deleted_at = source.readString();
        this.latitude = source.readString();
        this.longitude = source.readString();
        this.text = source.readString();
        this.country = source.readString();
        this.country_short = source.readString();
        this.place = source.readString();
        this.region = source.readString();
        this.poi = source.readByte() != 0 ;
    }

    public String getId() {
        return id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public String getDeleted_at() {
        return deleted_at;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getText() {
        return text;
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
        return poi;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // SAME ORDER AS IN Location(Parcel source)!
        dest.writeString(this.id);
        dest.writeString(this.created_at);
        dest.writeString(this.updated_at);
        dest.writeString(this.deleted_at);
        dest.writeString(this.latitude);
        dest.writeString(this.longitude);
        dest.writeString(this.text);
        dest.writeString(this.country);
        dest.writeString(this.country_short);
        dest.writeString(this.place);
        dest.writeString(this.region);
        dest.writeByte((byte) (this.poi ? 1 : 0));
    }

    public static final Parcelable.Creator<Location> CREATOR =
            new Parcelable.Creator<Location>() {

                @Override
                public Location createFromParcel(Parcel source) {
                    return new Location(source);
                }

                @Override
                public Location[] newArray(int size) {
                    return new Location[size];
                }
            };
}
