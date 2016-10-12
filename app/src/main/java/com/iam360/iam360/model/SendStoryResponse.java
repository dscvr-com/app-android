package com.iam360.iam360.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joven on 10/6/2016.
 */
public class SendStoryResponse{
    private SendStoryResponseData data;
    private String status;
    private String message;

    public SendStoryResponse(){
        data = new SendStoryResponseData();
        status = "";
        message = "";
    }


    public void setData(SendStoryResponseData data) {
        this.data = data;
    }

    public SendStoryResponseData getData() {
        return data;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public class SendStoryResponseData {
        private String story_id;
        private List<StoryChild> children;

        public SendStoryResponseData(){
            story_id = "";
            children = new ArrayList<StoryChild>();
        }

        public List<StoryChild> getChildren() {
            return children;
        }

        public void setChildren(List<StoryChild> children) {
            this.children = children;
        }

        public String getStory_id() {
            return story_id;
        }

        public void setStory_id(String story_id) {
            this.story_id = story_id;
        }
    }
}
