package com.errand.team5.errand;

import java.io.Serializable;

public class Notification implements Serializable {

    String nId;
    String message;
    String taskID;
    User requester;
    User creator;
    int type;
        //NEED_APPROVAL = 0 = needs approval
        public static final int NEEDS_APPROVAL = 0;
        public static final int PENDING_APPROVAL = 1;
        //2 = needs confirmation
        public static final int NEEDS_CONFIRMATION = 2;
        //3 = pending confirmation
        public static final int PENDING_CONFIRMATION = 3;
    //can add more if needed for "payout successful", etc
    int status;
        //0 = open (approval or confirmation still pending/action required)
        public static final int OPEN = 0;
        //1 = closed (approved or confirmed/no action required)
        public static final int CLOSED = 1;        //1 = pending approval

    public Notification(){

    }

    public Notification(String nId, String message, String taskID, User requester, User creator, int type, int status)
    {
        this.nId = nId;
        this.message = message;
        this.taskID = taskID;
        this.requester = requester;
        this.creator = creator;
        this.type = type;
        this.status = status;
    }



    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() { return status; }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getnId() {
        return nId;
    }

    public void setnId(String nId) {
        this.nId = nId;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }
  
    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) { this.creator = creator; }
}
