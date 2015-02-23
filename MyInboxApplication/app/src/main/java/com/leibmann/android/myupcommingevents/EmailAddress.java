//Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
//
package com.leibmann.android.myupcommingevents;

import java.io.Serializable;

/**
 * Created by mattleib on 1/26/2015.
 */
public class EmailAddress implements Serializable {

    protected String Address;
    protected String Name;

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public EmailAddress(String address, String name) {
        Address = address;
        Name = name;
    }
}
