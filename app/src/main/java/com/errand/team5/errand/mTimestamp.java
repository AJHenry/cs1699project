package com.errand.team5.errand;

/**
 * Created by Andrew on 3/31/2018.
 * Needed for the stupid no-argument constructor for Firebase
 */


public class mTimestamp extends java.sql.Timestamp {

    public mTimestamp(long time) {
        super(time);
    }

    public mTimestamp() {
        super(System.currentTimeMillis());

    }
}
