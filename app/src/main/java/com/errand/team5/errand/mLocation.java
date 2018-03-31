package com.errand.team5.errand;

/**
 * Created by Andrew on 3/31/2018.
 */

public class mLocation extends android.location.Location {

    public mLocation(){
        super("");
    }

    public mLocation(double lat, double lng){
        super("");
        super.setLatitude(lat);
        super.setLongitude(lng);
    }
}
