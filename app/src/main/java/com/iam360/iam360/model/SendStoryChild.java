package com.iam360.iam360.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joven on 10/6/2016.
 */
public class SendStoryChild implements Parcelable {
    private String story_object_id;
    private String story_object_media_type;
    private String story_object_media_face;
    private String story_object_media_description;
    private String story_object_media_additional_data;
    private List<String> story_object_position;
    private List<String> story_object_rotation;
    private String story_object_media_filename;
    private String story_object_media_fileurl;

    public SendStoryChild() {
        story_object_id = "";
        story_object_media_type = "";
        story_object_media_face = "";
        story_object_media_description = "";
        story_object_media_additional_data = "";
        story_object_position = new ArrayList<String>();
        story_object_rotation = new ArrayList<String>();
        story_object_media_filename = "";
        story_object_media_fileurl = "";
    }

    public SendStoryChild(Parcel source) {
        // SAME ORDER AS IN writeToParcel!
        this.story_object_id = source.readString();
        this.story_object_media_type = source.readString();
        this.story_object_media_face = source.readString();
        this.story_object_media_description = source.readString();
        this.story_object_media_additional_data = source.readString();
        this.story_object_position = source.readArrayList(String.class.getClassLoader());
        this.story_object_rotation = source.readArrayList(String.class.getClassLoader());
        this.story_object_media_filename = source.readString();
        this.story_object_media_fileurl = source.readString();
    }


    public String getStory_object_media_type() {
        return story_object_media_type;
    }

    public void setStory_object_media_type(String story_object_media_type) {
        this.story_object_media_type = story_object_media_type;
    }

    public String getStory_object_media_face() {
        return story_object_media_face;
    }

    public void setStory_object_media_face(String story_object_media_face) {
        this.story_object_media_face = story_object_media_face;
    }

    public String getStory_object_media_description() {
        return story_object_media_description;
    }

    public void setStory_object_media_description(String story_object_media_description) {
        this.story_object_media_description = story_object_media_description;
    }

    public String getStory_object_media_additional_data() {
        return story_object_media_additional_data;
    }

    public void setStory_object_media_additional_data(String story_object_media_additional_data) {
        this.story_object_media_additional_data = story_object_media_additional_data;
    }

    public List<String> getStory_object_position() {
        return story_object_position;
    }

    public void setStory_object_position(List<String> story_object_position) {
        this.story_object_position = story_object_position;
    }

    public List<String> getStory_object_rotation() {
        return story_object_rotation;
    }

    public void setStory_object_rotation(List<String> story_object_rotation) {
        this.story_object_rotation = story_object_rotation;
    }

    public String getStory_object_media_filename() {
        return story_object_media_filename;
    }

    public void setStory_object_media_filename(String story_object_media_filename) {
        this.story_object_media_filename = story_object_media_filename;
    }

    public String getStory_object_media_fileurl() {
        return story_object_media_fileurl;
    }

    public void setStory_object_media_fileurl(String story_object_media_fileurl) {
        this.story_object_media_fileurl = story_object_media_fileurl;
    }

    public String getStory_object_id() {
        return story_object_id;
    }

    public void setStory_object_id(String story_object_id) {
        this.story_object_id = story_object_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.story_object_id);
        dest.writeString(this.story_object_media_type);
        dest.writeString(this.story_object_media_face);
        dest.writeString(this.story_object_media_description);
        dest.writeString(this.story_object_media_additional_data);
        dest.writeList(story_object_position);
        dest.writeList(story_object_rotation);
        dest.writeString(this.story_object_media_filename);
        dest.writeString(this.story_object_media_fileurl);
    }

    public static final Creator<StoryChild> CREATOR =
            new Creator<StoryChild>() {

                @Override
                public StoryChild createFromParcel(Parcel source) {
                    return new StoryChild(source);
                }

                @Override
                public StoryChild[] newArray(int size) {
                    return new StoryChild[size];
                }
            };
}
