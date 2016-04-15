package co.optonaut.optonaut.model;

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
    private String text;
    private String avatar_asset_id;
    private int optographs_count;
    private int followers_count;
    private int followed_count;
    private boolean is_followed;

    public Person() {
        id = "";
        created_at = "";
        deleted_at = "";
        display_name = "";
        user_name = "";
        text = "";
        avatar_asset_id = "";
        optographs_count = 0;
        followers_count = 0;
        followed_count = 0;
        is_followed = false;
    }

    public Person(Parcel source) {
        // SAME ORDER AS IN writeToParcel!
        this.id = source.readString();
        this.created_at = source.readString();
        this.deleted_at = source.readString();
        this.display_name = source.readString();
        this.user_name = source.readString();
        this.text = source.readString();
        this.avatar_asset_id = source.readString();
        this.optographs_count = source.readInt();
        this.followers_count = source.readInt();
        this.followed_count = source.readInt();
        // see http://stackoverflow.com/a/7089687/1176596
        this.is_followed = source.readByte() != 0;
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
        dest.writeString(this.text);
        dest.writeString(this.avatar_asset_id);
        dest.writeInt(this.optographs_count);
        dest.writeInt(this.followers_count);
        dest.writeInt(this.followed_count);
        // see http://stackoverflow.com/a/7089687/1176596
        dest.writeByte((byte) (this.is_followed ? 1 : 0));
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
