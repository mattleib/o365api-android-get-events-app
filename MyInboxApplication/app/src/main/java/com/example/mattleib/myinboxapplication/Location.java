package com.example.mattleib.myinboxapplication;

import java.io.Serializable;

/**
 * Created by mattleib on 1/26/2015.
 */
public class Location implements Serializable {

    protected String DisplayName;

    public String getDisplayName() {
        return DisplayName;
    }

    public void setDisplayName(String displayName) {
        DisplayName = displayName;
    }

    public Location(String displayName)
    {
        this.DisplayName = displayName;
    }
}
