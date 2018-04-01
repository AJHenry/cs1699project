package com.errand.team5.errand;

import android.net.Uri;

import java.io.Serializable;
import java.nio.file.SecureDirectoryStream;

/**
 * Created by imand on 4/1/2018.
 */

public class User implements Serializable{
    String uid;
    String photoUrl;
    String displayName;
    String email;

    public User(){

    }

    public User(String uid, String photoUrl, String displayName, String email) {
        this.uid = uid;
        this.photoUrl = photoUrl;
        this.displayName = displayName;
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
