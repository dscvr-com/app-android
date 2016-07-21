package com.iam360.iam360.model;

/**
 * Created by Joven on 7/19/2016.
 */
public class NotificationTriggerData {
    String owner_id;
    String action_type;
    String follower_id;
    String optograph_id;

    public NotificationTriggerData(String oId, String fId, String opId, String type){
        this.owner_id = oId;
        this.follower_id = fId;
        this.optograph_id = opId;
        this.action_type = type;
    }
}
