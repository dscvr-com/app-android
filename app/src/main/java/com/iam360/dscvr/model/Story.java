package com.iam360.dscvr.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/*
    "story": {
        "id": "62f1c6cb-cb90-4ade-810d-c6c1bbeee85a",
        "created_at": "2016-08-05 07:35:55+00",
        "updated_at": "2016-08-05 07:35:55+00",
        "deleted_at": null,
        "optograph_id": "88a257df-2008-4d7b-ae44-7ea603011867",
        "person_id": "7753e6e9-23c6-46ec-9942-35a5ea744ece",
        "children": [
            {
            "story_object_id": "58ddff09-dd8a-4e5e-9054-aa9d02ff1604",
            "story_object_story_id": "62f1c6cb-cb90-4ade-810d-c6c1bbeee85a",
            "story_object_media_type": "NAV",
            "story_object_media_face": "pin",
            "story_object_media_description": "description",
            "story_object_media_additional_data": "8e8fcab1-761e-4ef8-a969-0c1346b39bf3",
            "story_object_position": [
            "0.6512613",
            "0.7435949",
            "-0.1514112"
            ],
            "story_object_rotation": [
            "1.4188",
            "-1.192093e-07",
            "-0.7192987"
            ],
            "story_object_created_at": "2016-08-05 07:35:55+00",
            "story_object_updated_at": "2016-08-05 07:35:55+00",
            "story_object_deleted_at": null,
            "story_object_media_filename": "",
            "story_object_media_fileurl": ""
            }
        ]
    }
 */

public class Story implements Parcelable {
    private String id;
    private String created_at;
    private String updated_at;
    private String deleted_at;
    private String optograph_id;
    private String person_id;
    private List<StoryChild> children;

    public Story() {
        id = "";
        created_at = "";
        updated_at = "";
        deleted_at = "";
        optograph_id = "";
        person_id = "";
        children = null;
    }

    public Story(Parcel source) {
        // SAME ORDER AS IN writeToParcel!
        this.id = source.readString();
        this.created_at = source.readString();
        this.updated_at = source.readString();
        this.deleted_at = source.readString();
        this.optograph_id = source.readString();
        this.person_id = source.readString();
        this.children = source.readArrayList(StoryChild.class.getClassLoader());
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

    public String getOptograph_id() {
        return optograph_id;
    }

    public void setOptograph_id(String optograph_id) {
        this.optograph_id = optograph_id;
    }

    public String getPerson_id() {
        return person_id;
    }

    public void setPerson_id(String person_id) {
        this.person_id = person_id;
    }

    public List<StoryChild> getChildren() {
        return children;
    }

    public void setChildren(List<StoryChild> children) {
        this.children = children;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.created_at);
        dest.writeString(this.updated_at);
        dest.writeString(this.deleted_at);
        dest.writeString(this.optograph_id);
        dest.writeString(this.person_id);
        dest.writeList(children);
    }

    public static final Creator<Story> CREATOR =
            new Creator<Story>() {

                @Override
                public Story createFromParcel(Parcel source) {
                    return new Story(source);
                }

                @Override
                public Story[] newArray(int size) {
                    return new Story[size];
                }
            };
}
