package com.iam360.iam360.model;

/**
 * Created by Mariel on 6/27/2016.
 */

/*{
"id": "7753e6e9-23c6-46ec-9942-35a5ea744ece",
"created_at": "2016-06-24T09:52:14.910835Z",
"updated_at": "2016-06-24T09:52:14.910835Z",
"deleted_at": null,
"wants_newsletter": true,
"email": "lebron_gtteuxa_james@tfbnw.net",
"display_name": "Lebron",
"user_name": "3d2a03ef2758477f8cacc39b90343b3e",
"text": "",
"avatar_asset_id": "dad5bea1-ed0d-4782-b0c4-a9ce1714abf8",
"optographs": null,
"optographs_count": 0,
"followers_count": 0,
"followed_count": 0,
"is_followed": false
}*/
public class Follower {
    private String id;
    private String created_at;
    private String updated_at;
    private String deleted_at;
    private boolean wants_newsletter;
    private String email;
    private String display_name;
    private String user_name;
    private String text;
    private String avatar_asset_id;
    private int optographs_count;
    private int followers_count;
    private int followed_count;
    private boolean is_followed;

    public Follower() {
        id = "";
        created_at = "";
        updated_at = "";
        deleted_at = "";
        wants_newsletter = false;
        email = "";
        display_name = "";
        user_name = "";
        text = "";
        avatar_asset_id = "";
        optographs_count = 0;
        followers_count = 0;
        followed_count = 0;
        is_followed = false;
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

    public boolean isWants_newsletter() {
        return wants_newsletter;
    }

    public void setWants_newsletter(boolean wants_newsletter) {
        this.wants_newsletter = wants_newsletter;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAvatar_asset_id() {
        return avatar_asset_id;
    }

    public void setAvatar_asset_id(String avatar_asset_id) {
        this.avatar_asset_id = avatar_asset_id;
    }

    public int getOptographs_count() {
        return optographs_count;
    }

    public void setOptographs_count(int optographs_count) {
        this.optographs_count = optographs_count;
    }

    public int getFollowers_count() {
        return followers_count;
    }

    public void setFollowers_count(int followers_count) {
        this.followers_count = followers_count;
    }

    public int getFollowed_count() {
        return followed_count;
    }

    public void setFollowed_count(int followed_count) {
        this.followed_count = followed_count;
    }

    public boolean is_followed() {
        return is_followed;
    }

    public void setIs_followed(boolean is_followed) {
        this.is_followed = is_followed;
    }
}
