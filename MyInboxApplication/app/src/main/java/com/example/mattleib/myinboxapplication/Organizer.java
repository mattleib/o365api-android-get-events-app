package com.example.mattleib.myinboxapplication;

import java.io.Serializable;

/**
 * Created by mattleib on 1/26/2015.
 */
public class Organizer implements Serializable {

    protected EmailAddress EmailAddress;

    public EmailAddress getEmailAddress() {
        return EmailAddress;
    }

    public void setEmailAddress(EmailAddress emailAddress) {
        EmailAddress = emailAddress;
    }

    public Organizer(com.example.mattleib.myinboxapplication.EmailAddress emailAddress) {
        EmailAddress = emailAddress;
    }
}
