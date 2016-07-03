package com.iam360.iam360.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Nilan Marktanner
 * @date 2015-12-04
 */

/*
"person":{
"id":"1064fd0e-833b-4a6b-b4bc-d90a03074eba",
"created_at":"2015-09-28T16:23:26.871373Z",
"deleted_at":null,"wants_newsletter":false,
"display_name":"Emanuel",
"user_name":"emi",
"text":"@optonaut cofounder, dev, entrepreneur, photographer, KIT MSc student",
"avatar_asset_id":"5d8793da-0325-4655-b213-5ec97cd7f478",
"optographs":null,
"optographs_count": 0,
"followers_count":23,
"followed_count":22,
"is_followed":false
}


 */
public class Person implements Parcelable {
    private String id;
    private String created_at;
    private String deleted_at;
    private String display_name;
    private String user_name;
    private String email;
    private String text;
    private String avatar_asset_id;
    private int optographs_count;
    private int followers_count;
    private int followed_count;
    private boolean is_followed;
    private String facebook_user_id;
    private String facebook_token;
    private String twitter_token;
    private String twitter_secret;

    public Person() {
        id = "";
        created_at = "";
        deleted_at = "";
        display_name = "";
        user_name = "";
        email = "";
        text = "";
        avatar_asset_id = "";
        optographs_count = 0;
        followers_count = 0;
        followed_count = 0;
        is_followed = false;
        facebook_user_id = "";
        facebook_token = "";
        twitter_token = "";
        twitter_secret = "";
    }

    public Person(Parcel source) {
        // SAME ORDER AS IN writeToParcel!
        this.id = source.readString();
        this.created_at = source.readString();
        this.deleted_at = source.readString();
        this.display_name = source.readString();
        this.user_name = source.readString();
        this.email = source.readString();
        this.text = source.readString();
        this.avatar_asset_id = source.readString();
        this.optographs_count = source.readInt();
        this.followers_count = source.readInt();
        this.followed_count = source.readInt();
        // see http://stackoverflow.com/a/7089687/1176596
        this.is_followed = source.readByte() != 0;
        this.facebook_user_id = source.readString();
        this.facebook_token = source.readString();
        this.twitter_token = source.readString();
        this.twitter_secret = source.readString();
    }

    public String getId() {
        return id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getDeleted_at() {
        return deleted_at;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getText() {
        return text;
    }

    public String getAvatar_asset_id() {
        return avatar_asset_id;
    }

    public int getOptographs_count() { return optographs_count; }

    public int getFollowers_count() {
        return followers_count;
    }

    public int getFollowed_count() {
        return followed_count;
    }

    public boolean is_followed() {
        return is_followed;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setAvatar_asset_id(String avatar_asset_id) {
        this.avatar_asset_id = avatar_asset_id;
    }

    public void setOptographs_count(int optographs_count) {
        this.optographs_count = optographs_count;
    }

    public void setFollowers_count(int followers_count) {
        this.followers_count = followers_count;
    }

    public void setFollowed_count(int followed_count) {
        this.followed_count = followed_count;
    }

    public void setIs_followed(boolean is_followed) {
        this.is_followed = is_followed;
    }

    public String getTwitter_secret() {
        return twitter_secret;
    }

    public void setTwitter_secret(String twitter_secret) {
        this.twitter_secret = twitter_secret;
    }

    public String getTwitter_token() {
        return twitter_token;
    }

    public void setTwitter_token(String twitter_token) {
        this.twitter_token = twitter_token;
    }

    public String getFacebook_token() {
        return facebook_token;
    }

    public void setFacebook_token(String facebook_token) {
        this.facebook_token = facebook_token;
    }

    public String getFacebook_user_id() {
        return facebook_user_id;
    }

    public void setFacebook_user_id(String facebook_user_id) {
        this.facebook_user_id = facebook_user_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // SAME ORDER AS IN Person(Parcel source)!
        dest.writeString(this.id);
        dest.writeString(this.created_at);
        dest.writeString(this.deleted_at);
        dest.writeString(this.display_name);
        dest.writeString(this.user_name);
        dest.writeString(this.email);
        dest.writeString(this.text);
        dest.writeString(this.avatar_asset_id);
        dest.writeInt(this.optographs_count);
        dest.writeInt(this.followers_count);
        dest.writeInt(this.followed_count);
        // see http://stackoverflow.com/a/7089687/1176596
        dest.writeByte((byte) (this.is_followed ? 1 : 0));
        dest.writeString(this.facebook_user_id);
        dest.writeString(this.facebook_token);
        dest.writeString(this.twitter_token);
        dest.writeString(this.twitter_secret);
    }

    public static final Parcelable.Creator<Person> CREATOR =
            new Parcelable.Creator<Person>() {

                @Override
                public Person createFromParcel(Parcel source) {
                    return new Person(source);
                }

                @Override
                public Person[] newArray(int size) {
                    return new Person[size];
                }
            };
}
