package com.errand.team5.errand;

import java.time.Instant;

/**
 * Created by Andrew on 3/31/2018.
 * Needed for the stupid no-argument constructor for Firebase
 */


public class mTimestamp extends java.sql.Timestamp {

    public mTimestamp(long time) {
        super(time);
    }

    public mTimestamp() {
        //Doesnt work with differnt time zones
        super(System.currentTimeMillis());
        //super(Instant.now().toEpochMilli());

    }
}
