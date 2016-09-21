package com.iam360.dscvr.model;

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
    private double latitude;
    private double longitude;
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
        latitude = 0;
        longitude = 0;
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
        this.latitude = source.readDouble();
        this.longitude = source.readDouble();
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

    public void setId(String id) {
        this.id = id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getDeleted_at() {
        return deleted_at;
    }

    public void setDeleted_at(String deleted_at) {
        this.deleted_at = deleted_at;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry_short() {
        return country_short;
    }

    public void setCountry_short(String country_short) {
        this.country_short = country_short;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isPoi() {
        return poi;
    }

    public void setPoi(boolean poi) {
        this.poi = poi;
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
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
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
