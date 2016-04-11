package co.optonaut.optonaut.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import co.optonaut.optonaut.util.RFC3339DateFormatter;

/**
 * @author Nilan Marktanner
 * @date 2015-11-13
 */

/*
{
"id":"ae43aee0-6732-4644-859e-81857fcd9b42",
"created_at":"2015-11-25T15:40:45.838Z",
"deleted_at":null,
"stitcher_version":"0.6.0",
"text":"Skies of London II",
"views_count":12,
"is_staff_pick":true,
"share_alias":"r4ioki",
"is_private":false,
"preview_asset_id":"7eb94e5e-1a38-4d92-8ead-956c86468693",
"left_texture_asset_id":"a1dae0e6-f4e7-4137-a746-aed6a5d39b00",
"right_texture_asset_id":"d7502ac3-de68-4438-9cb6-b1e5ec1b6185",
"location":{"id":"78b7fa88-4f27-4012-ad94-5229356c763a",
    "created_at":"2015-11-25T15:41:03.358Z",
    "deleted_at":null,"latitude":51.47981643676758,
    "longitude":-0.08365650475025177,
    "text":"Camberwell",
    "country":"United Kingdom"},
"person":{"id":"1064fd0e-833b-4a6b-b4bc-d90a03074eba",
    "created_at":"2015-09-28T16:23:26.871373Z",
    "deleted_at":null,
    "wants_newsletter":false,
    "display_name":"Emanuel",
    "user_name":"emi",
    "text":"@optonaut cofounder, dev, entrepreneur, photographer, KIT MSc student",
    "avatar_asset_id":"5d8793da-0325-4655-b213-5ec97cd7f478",
    "optographs":null,
    "followers_count":23,
    "followed_count":22,
    "is_followed":false},
"stars_count":4,
"comments_count":1,
"is_starred":false,
"hashtag_string":"london"}


 */

public class Optograph implements Parcelable {
    private String id;
    private String created_at;
    private String deleted_at;
    private String stitcher_version;
    private String text;
    private int views_count;
    private boolean is_staff_picked;
    private String share_alias;
    private boolean is_private;
    private String preview_asset_id;
    private String left_texture_asset_id;
    private String right_texture_asset_id;
    private Location location;
    private Person person;
    private int stars_count;
    private int comments_count;
    private boolean is_starred;
    private String hashtag_string;

    // default value for parsing from JSON
    private boolean is_local = false;

    public Optograph(String id) {
        this.id = id;
        created_at = RFC3339DateFormatter.toRFC3339String(DateTime.now());
        deleted_at = "";
        stitcher_version = "";
        text = "";
        views_count = 0;
        is_staff_picked = false;
        share_alias = "";
        is_private = false;
        preview_asset_id = "";
        left_texture_asset_id = "";
        right_texture_asset_id = "";
        location = new Location();
        person = new Person();
        stars_count = 0;
        is_starred = false;
        hashtag_string = "";
        is_local = true;
    }


    public Optograph(Parcel source) {
        // SAME ORDER AS IN writeToParcel!
        this.id = source.readString();
        this.created_at = source.readString();
        this.deleted_at = source.readString();
        this.stitcher_version = source.readString();
        this.text = source.readString();
        this.views_count = source.readInt();
        this.is_staff_picked = source.readByte() != 0 ;
        this.share_alias = source.readString();
        this.is_private = source.readByte() != 0 ;
        this.preview_asset_id = source.readString();
        this.left_texture_asset_id = source.readString();
        this.right_texture_asset_id = source.readString();
        this.location = source.readParcelable(Location.class.getClassLoader());
        this.person = source.readParcelable(Person.class.getClassLoader());
        this.stars_count = source.readInt();
        this.comments_count = source.readInt();
        this.is_starred = source.readByte() != 0;
        this.hashtag_string = source.readString();
        this.is_local = source.readByte() != 0;
    }

    public String getId() {
        return id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public DateTime getCreated_atDateTime() {
        return RFC3339DateFormatter.fromRFC3339String(getCreated_at());
    }

    public String getCreated_atRFC3339() {
        String date = getCreated_at();
        String convDate = getCreated_at();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            convDate = format.format(formatter.parse(date));
        } catch (ParseException e) {
            Log.e("myTag","Error parsing created_at");
        }
        return convDate;
    }

    public String getDeleted_at() {
        return deleted_at;
    }

    public String getStitcher_version() {
        return stitcher_version;
    }

    public String getText() {
        return text;
    }

    public int getViews_count() {
        return views_count;
    }

    public boolean is_staff_picked() {
        return is_staff_picked;
    }

    public String getShare_alias() {
        return share_alias;
    }

    public boolean is_private() {
        return is_private;
    }

    public String getPreview_asset_id() {
        return preview_asset_id;
    }

    public String getLeft_texture_asset_id() {
        return left_texture_asset_id;
    }

    public String getRight_texture_asset_id() {
        return right_texture_asset_id;
    }

    public Location getLocation() {
        return location;
    }

    public Person getPerson() {
        return person;
    }

    public int getStars_count() {
        return stars_count;
    }

    public int getComments_count() {
        return comments_count;
    }

    public boolean is_starred() {
        return is_starred;
    }

    public String getHashtag_string() {
        return hashtag_string;
    }

    public boolean is_local() {
        return is_local;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Optograph optograph = (Optograph) o;

        return id.equals(optograph.id);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (left_texture_asset_id != null ? left_texture_asset_id.hashCode() : 0);
        result = 31 * result + (preview_asset_id != null ? preview_asset_id.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (right_texture_asset_id != null ? right_texture_asset_id.hashCode() : 0);
        result = 31 * result + (created_at != null ? created_at.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // SAME ORDER AS IN Optograph(Parcel source)!
        dest.writeString(this.id);
        dest.writeString(this.created_at);
        dest.writeString(this.deleted_at);
        dest.writeString(this.stitcher_version);
        dest.writeString(this.text);
        dest.writeInt(this.views_count);
        dest.writeByte((byte) (this.is_staff_picked ? 1 : 0));
        dest.writeString(this.share_alias);
        dest.writeByte((byte) (this.is_private ? 1 : 0));
        dest.writeString(this.preview_asset_id);
        dest.writeString(this.left_texture_asset_id);
        dest.writeString(this.right_texture_asset_id);
        dest.writeParcelable(this.location, flags);
        dest.writeParcelable(this.person, flags);
        dest.writeInt(this.stars_count);
        dest.writeInt(this.comments_count);
        dest.writeByte((byte) (this.is_starred ? 1 : 0));
        dest.writeString(this.hashtag_string);
        dest.writeByte((byte) (this.is_local ? 1:0));
    }

    public static final Parcelable.Creator<Optograph> CREATOR =
            new Parcelable.Creator<Optograph>() {

                @Override
                public Optograph createFromParcel(Parcel source) {
                    return new Optograph(source);
                }

                @Override
                public Optograph[] newArray(int size) {
                    return new Optograph[size];
                }
            };
}
