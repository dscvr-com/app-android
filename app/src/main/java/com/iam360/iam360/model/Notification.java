package com.iam360.iam360.model;

/**
 * Created by Mariel on 7/20/2016.
 */

/*FOLLOW*/
//{
//"id": "f9cc7b93-c2ed-4243-ab09-e43675689c01",
//"created_at": "2016-07-19T10:46:10.018811Z",
//"updated_at": "2016-07-19T10:46:10.018811Z",
//"deleted_at": null,
//"type": "follow",
//"is_read": false,
//"person": null,
//"activity_resource_star": null,
//"activity_resource_comment": null,
//"activity_resource_follow": {
//  "id": "43943a21-55df-439b-ad44-64b603b3f050",
//  "causing_person": {
//      "id": "7bf6b567-9d32-4d12-8f3b-9925349c66ff",
//      "created_at": "2016-07-03T03:26:26.149078Z",
//      "updated_at": "0001-01-01T00:00:00Z",
//      "deleted_at": null,
//      "wants_newsletter": false,
//      "display_name": "Will",
//      "user_name": "will1",
//      "text": "",
//      "elite_status": false,
//      "avatar_asset_id": "9f147e3d-5055-4932-a2d8-1741ff25d543",
//      "optographs": null,
//      "optographs_count": 9,
//      "followers_count": 2,
//      "followed_count": 3,
//      "is_followed": true
//  }
//},
//"activity_resource_views": null
//}

/*STAR*/
//{
//"id": "e85a91c0-d83c-4f43-9d94-314e8574b6a9",
//"created_at": "2016-07-20T13:07:26.585606Z",
//"updated_at": "2016-07-20T13:07:26.585606Z",
//"deleted_at": null,
//"type": "star",
//"is_read": false,
//"person": null,
//"activity_resource_star": {
//  "id": "f52293cd-ace8-48e1-a881-b54b31d6b0c7",
//  "optograph": {
//      "id": "9d5c9544-b119-4524-bc8e-22f146567c0e",
//      "created_at": "2016-05-20T08:10:29.99Z",
//      "updated_at": "2016-05-20T08:11:02.42866Z",
//      "deleted_at": null,
//      "stitcher_version": "0.7.0",
//      "text": "",
//      "views_count": 0,
//      "optograph_type": "optograph",
//      "optograph_platform": "-",
//      "optograph_model": "-",
//      "optograph_make": "-",
//      "optograph_daemon": "",
//      "is_staff_pick": false,
//      "share_alias": "f2ggnt",
//      "is_private": false,
//      "direction_phi": 0,
//      "direction_theta": 0,
//      "is_published": true,
//      "placeholder_texture_asset_id": "",
//      "left_texture_asset_id": "00000000-0000-0000-0000-000000000000",
//      "right_texture_asset_id": "00000000-0000-0000-0000-000000000000",
//      "location": null,
//      "person": null,
//      "stars_count": 0,
//      "comments_count": 0,
//      "is_starred": false,
//      "hashtag_string": ""
//  },
//  "causing_person": {
//      "id": "d66d3048-48cf-49a1-ba7b-b8b2ca931dd5",
//      "created_at": "2016-07-11T10:48:08.851829Z",
//      "updated_at": "0001-01-01T00:00:00Z",
//      "deleted_at": null,
//      "wants_newsletter": false,
//      "display_name": "junifer",
//      "user_name": "junifer",
//      "text": "",
//      "elite_status": false,
//      "avatar_asset_id": "7c7ad145-a81c-48ca-976d-b2980d99fc66",
//      "optographs": null,
//      "optographs_count": 3,
//      "followers_count": 2,
//      "followed_count": 2,
//      "is_followed": false
//  }
//},
//"activity_resource_comment": null,
//"activity_resource_follow": null,
//"activity_resource_views": null
//}
public class Notification {
    private String id;
    private String created_at;
    private String updated_at;
    private String deleted_at;
    private String type;
    private boolean is_read;
    private Person person;
    private Star activity_resource_star;
    private Comment activity_resource_comment;
    private Follow activity_resource_follow;
    private Views activity_resource_views;

    public Notification() {
        id="";
        created_at="";
        updated_at="";
        deleted_at="";
        type="";
        is_read=false;
        person= new Person();
        activity_resource_star=new Star();
        activity_resource_comment=new Comment();
        activity_resource_follow=new Follow();
        activity_resource_views=new Views();
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

    public String getType() {
        return type;
    }

    public boolean isIs_read() {
        return is_read;
    }

    public void setIs_read(boolean is_read) {
        this.is_read = is_read;
    }

    public Person getPerson() {
        return person;
    }

    public Star getActivity_resource_star() {
        return activity_resource_star;
    }

    public Comment getActivity_resource_comment() {
        return activity_resource_comment;
    }

    public Follow getActivity_resource_follow() {
        return activity_resource_follow;
    }

    public Views getActivity_resource_views() {
        return activity_resource_views;
    }

    public class Follow {
        private String id;
        private Person causing_person;

        public Follow() {
            id="";
            causing_person=new Person();
        }

        public String getId() {
            return id;
        }

        public Person getCausing_person() {
            return causing_person;
        }
    }

    public class Star {
        private String id;
        private Optograph optograph;
        private Person causing_person;

        public Star() {
            id="";
            optograph=null;
            causing_person = new Person();
        }

        public String getId() {
            return id;
        }

        public Optograph getOptograph() {
            return optograph;
        }

        public Person getCausing_person() {
            return causing_person;
        }
    }

    class Comment {

    }

    class Views {

    }
}
