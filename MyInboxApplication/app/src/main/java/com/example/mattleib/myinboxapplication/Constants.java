package com.example.mattleib.myinboxapplication;

/**
 * Created by mattleib on 1/23/2015.
 */
public class Constants {

    public static String AAD_Client_ID ="af9ef21e-b71b-4b01-9eb6-1d16a8c9ed22";

    //public static String AAD_RedirectUri = "app://com.example.mattleib.myinboxapplication";
    public static String AAD_RedirectUri = "https://myinboxapplication";

    public static String AAD_Authority = "https://login.windows.net/common";

    public static String O365_ExchangeOnline = "https://outlook.office365.com/";

    public static String O365_UserHint = "garthf@oauthplay.onmicrosoft.com";

    public static String O365_EventsQueryTemplate = "https://outlook.office365.com/api/v1.0/me/calendarview?startdatetime=%sT%sZ&enddatetime=%sT%sZ&$top=50&$orderby=Start";
}

