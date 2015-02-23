//Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
//
package com.leibmann.android.myupcommingevents;

import java.io.Serializable;

/**
 * Created by mattleib on 1/26/2015.
 */
public class Organizer implements Serializable {

    protected com.leibmann.android.myupcommingevents.EmailAddress EmailAddress;

    public com.leibmann.android.myupcommingevents.EmailAddress getEmailAddress() {
        return EmailAddress;
    }

    public void setEmailAddress(com.leibmann.android.myupcommingevents.EmailAddress emailAddress) {
        EmailAddress = emailAddress;
    }

    public Organizer(com.leibmann.android.myupcommingevents.EmailAddress emailAddress) {
        EmailAddress = emailAddress;
    }
}
