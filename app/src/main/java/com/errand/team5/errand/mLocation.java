package com.errand.team5.errand;

import java.io.Serializable;

/**
 * Created by Andrew on 3/31/2018.
 */

public class mLocation extends android.location.Location implements Serializable{

    public mLocation(){
        super("");
    }

    public mLocation(double lat, double lng){
        super("");
        super.setLatitude(lat);
        super.setLongitude(lng);
    }
}
