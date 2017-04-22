package com.iam360.dscvr.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Joven on 10/6/2016.
 */
public class SendStory implements Parcelable {
    private String story_optograph_id;
    private String story_person_id;
    private List<SendStoryChild> children;

    public SendStory() {
        story_optograph_id = "";
        story_person_id = "";
        children = null;
    }

    public SendStory(Parcel source) {
        // SAME ORDER AS IN writeToParcel!
        this.story_optograph_id = source.readString();
        this.story_person_id = source.readString();
        this.children = source.readArrayList(StoryChild.class.getClassLoader());
    }

    public String getStory_optograph_id() {
        return story_optograph_id;
    }

    public void setStory_optograph_id(String story_optograph_id) {
        this.story_optograph_id = story_optograph_id;
    }

    public String getStory_person_id() {
        return story_person_id;
    }

    public void setStory_person_id(String story_person_id) {
        this.story_person_id = story_person_id;
    }

    public List<SendStoryChild> getChildren() {
        return children;
    }

    public void setChildren(List<SendStoryChild> children) {
        this.children = children;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.story_optograph_id);
        dest.writeString(this.story_person_id);
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
