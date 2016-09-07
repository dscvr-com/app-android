package com.iam360.iam360.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/*
{
"feed": [
    {
    "placeholder": "https://bucket.dscvr.com/textures/35b90650-67b0-4028-99ef-49a42ecdf9a8/l0.jpg",
    "id": "35b90650-67b0-4028-99ef-49a42ecdf9a8",
    "created_at": "2016-04-06 13:45:37.993+00",
    "updated_at": "2016-04-06 13:45:38.884994+00",
    "deleted_at": null,
    "stitcher_version": "0.7.0",
    "text": "",
    "views_count": 0,
    "optograph_type": "optograph_1",
    "optograph_platform": "-",
    "optograph_model": "-",
    "optograph_make": "-",
    "optograph_daemon": "",
    "is_staff_pick": true,
    "share_alias": "fi81p5",
    "is_private": false,
    "direction_phi": 0,
    "direction_theta": 0,
    "is_published": false,
    "placeholder_texture_asset_id": "",
    "left_texture_asset_id": "00000000-0000-0000-0000-000000000000",
    "right_texture_asset_id": "00000000-0000-0000-0000-000000000000",
    "location": {
    "id": "00000000-0000-0000-0000-000000000000",
    "created_at": "2015-11-25 21:03:00.694582+00",
    "updated_at": "2015-11-25 21:03:00.694582+00",
    "deleted_at": null,
    "latitude": "0",
    "longitude": "0",
    "text": "Not available",
    "country": "Not available",
    "country_short": "",
    "place": "",
    "region": "",
    "poi": true
    },
    "person": {
        "id": "c0d5cb2b-7f8a-4de9-a5de-6f7c6cf1cf1a",
        "created_at": "2016-04-06 13:44:43.27781+00",
        "updated_at": "2016-09-05 10:54:06.19193+00",
        "deleted_at": null,
        "wants_newsletter": null,
        "display_name": "wowowee",
        "user_name": "wowowee",
        "text": "heller worldhdhdjsjejsjsjsbsbsbhshshshbdhdhsj hehe heheh dudheheh dhehhe hehehhe shushed hehehhe hehehhe dhehhe end jejejjejecjcjckvkvjvjjfudjchchcchchchhsshsbsjsjwnksbzjwnshsnzhsnzjnsbsnsjbsksksbsjsnsjsbnsbnsnsb djjdjdd djdbdjdjdjjdsjjdjd Ddjdjjdjdjd djdjdjd djdjdjjdjd",
        "onboarding_version": "1",
        "elite_status": "t",
        "avatar_asset_id": "bd57c90a-c62c-4f0a-884f-47b29589f118",
        "optographs": null,
        "optographs_count": 0,
        "followers_count": "8",
        "followed_count": "7",
        "is_followed": false
    },
    "stars_count": "0",
    "comments_count": "0",
    "is_starred": false,
    "hashtag_string": "",
    "story": {
        "id": "c5be62d9-9cf7-429a-936a-0de361bbed96",
        "created_at": "2016-08-24 12:48:11+00",
        "updated_at": "2016-08-24 12:48:11+00",
        "deleted_at": null,
        "optograph_id": "35b90650-67b0-4028-99ef-49a42ecdf9a8",
        "person_id": "c0d5cb2b-7f8a-4de9-a5de-6f7c6cf1cf1a",
        "children": [
            {
                "story_object_id": "0efead8d-f6a4-4eb9-b0c3-cb453d7d2760",
                "story_object_story_id": "c5be62d9-9cf7-429a-936a-0de361bbed96",
                "story_object_media_type": "Image",
                "story_object_media_face": "Yo",
                "story_object_media_description": "Desc 1",
                "story_object_media_additional_data": "Data 1",
                "story_object_position": [
                "123",
                "456",
                "789"
                ],
                "story_object_rotation": [
                "987",
                "654",
                "321"
                ],
                "story_object_created_at": "2016-08-24 12:48:11+00",
                "story_object_updated_at": "2016-08-24 12:48:11+00",
                "story_object_deleted_at": null,
                "story_object_media_filename": "Thadz.mp3",
                "story_object_media_fileurl": "/stories/c5be62d9-9cf7-429a-936a-0de361bbed96/0efead8d-f6a4-4eb9-b0c3-cb453d7d2760/Thadz.mp3"
            }
        ]
    }
    }
    ],
"you": [
    {
    "placeholder": "https://bucket.dscvr.com/textures/88a257df-2008-4d7b-ae44-7ea603011867/l0.jpg",
    "id": "88a257df-2008-4d7b-ae44-7ea603011867",
    "created_at": "2016-06-28 08:01:58.833+00",
    "updated_at": "2016-06-28 08:18:11.52039+00",
    "deleted_at": null,
    "stitcher_version": "0.7.0",
    "text": "Wew",
    "views_count": 0,
    "optograph_type": "optograph_1",
    "optograph_platform": "iOS 9.3.2",
    "optograph_model": "iPhone 6",
    "optograph_make": "-",
    "optograph_daemon": "",
    "is_staff_pick": false,
    "share_alias": "mq00i5",
    "is_private": false,
    "direction_phi": 6,
    "direction_theta": -1,
    "is_published": true,
    "placeholder_texture_asset_id": "",
    "left_texture_asset_id": "00000000-0000-0000-0000-000000000000",
    "right_texture_asset_id": "00000000-0000-0000-0000-000000000000",
    "location": {
        "id": "4ff069f6-755e-4929-aee1-d4cfc7650e52",
        "created_at": "2016-06-28 08:18:12.332409+00",
        "updated_at": "2016-06-28 08:18:12.33241+00",
        "deleted_at": null,
        "latitude": "14.555",
        "longitude": "121.021",
        "text": "Manila",
        "country": "Philippines",
        "country_short": "PH",
        "place": "Manila",
        "region": "Metro Manila",
        "poi": false
    },
    "person": {
        "id": "7753e6e9-23c6-46ec-9942-35a5ea744ece",
        "created_at": "2016-06-24 09:52:14.910835+00",
        "updated_at": "2016-07-15 12:14:22+00",
        "deleted_at": null,
        "wants_newsletter": null,
        "display_name": "thadzdingo",
        "user_name": "thadzdingo",
        "text": "",
        "onboarding_version": "1",
        "elite_status": "t",
        "avatar_asset_id": "dad5bea1-ed0d-4782-b0c4-a9ce1714abf8",
        "optographs": null,
        "optographs_count": 0,
        "followers_count": "2",
        "followed_count": "3",
        "is_followed": false
    },
    "stars_count": "1",
    "comments_count": "0",
    "is_starred": false,
    "hashtag_string": "",
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
    }
    ]
}
 */

public class StoryFeed implements Parcelable {
    private List<Optograph> feed;
    private List<Optograph> you;

    public StoryFeed() {
        feed = null;
        you = null;
    }

    public StoryFeed(Parcel source) {
        this.feed = source.readArrayList(Optograph.class.getClassLoader());
        this.you = source.readArrayList(Optograph.class.getClassLoader());
    }

    public List<Optograph> getFeed() {
        return feed;
    }

    public void setFeed(List<Optograph> feed) {
        this.feed = feed;
    }

    public List<Optograph> getYou() {
        return you;
    }

    public void setYou(List<Optograph> you) {
        this.you = you;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(feed);
        dest.writeList(you);
    }

    public static final Creator<StoryFeed> CREATOR =
            new Creator<StoryFeed>() {

                @Override
                public StoryFeed createFromParcel(Parcel source) {
                    return new StoryFeed(source);
                }

                @Override
                public StoryFeed[] newArray(int size) {
                    return new StoryFeed[size];
                }
            };
}
