//Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
//
package com.leibmann.android.myupcommingevents;

/**
 * Created by mattleib on 1/23/2015.
 */
public class Constants {

    //
    // Production Values
    //
    public static final String AAD_Client_ID ="af9ef21e-b71b-4b01-9eb6-1d16a8c9ed22";
    //public static String AAD_RedirectUri = "app://com.example.mattleib.myinboxapplication";
    public static final String AAD_RedirectUri = "https://myinboxapplication";
    public static final String AAD_Authority = "https://login.windows.net/";
    public static final String O365_ExchangeOnline = "https://outlook.office365.com/";
    public static final String O365_UserHint = "";
    public static final String O365_SendEmailUri = "https://outlook.office365.com/api/v1.0/me/sendmail";
    public static final String O365_EventsQueryTemplate = "https://outlook.office365.com/api/v1.0/me/calendarview?startdatetime=%s&enddatetime=%s&$orderby=Start";

    //
    // PPE Values
    //
    public static final String PPE_AAD_Client_ID ="658a894e-1b57-40da-a350-061462eb5b77";
    public static final String PPE_AAD_RedirectUri = "https://myinboxapplication";
    public static final String PPE_AAD_Authority = "https://login.windows-ppe.net/";
    public static final String PPE_O365_ExchangeOnline = "https://sdfpilot.outlook.com/";
    public static final String PPE_O365_UserHint = "";
    public static final String PPE_O365_SendEmailUri = "https://sdfpilot.outlook.com/api/v1.0/me/sendmail";
    public static final String PPE_O365_EventsQueryTemplate = "https://sdfpilot.outlook.com/api/v1.0/me/calendarview?startdatetime=%s&enddatetime=%s&$orderby=Start";

    //
    // Program Runtime constants
    //
    public static class PreferenceKeys {
        public static final String CalendarTimeSpan = "PREF_CALENDAR_SPAN";
        public static final String UsePPE = "PREF_USEPPE";
        public static final String UseCoolColors = "PREF_USECOOL";
        public static final String RefreshToken = "PREF_REFRESHTOKEN";
        public static final String UserTenant = "PREF_AUTHTENANT";
        public static final String DoNotShowPastEvents = "PREF_NOPASTEVENTS";
        public static final String DoNotifications = "PREF_DONOTIFICATIONS";
    }

    public static final String PREFS_NAME = "MyPrefsFile";

    public static final String UserTenantDefault = "common";

    public static final int PICK_PREFERENCE_REQUEST = 1;
    public static final int IDX_PROD = 0;
    public static final int IDX_PPE = 1;

    public static final int NO_ITEM_SELECTED = -1;

    public static final long OneMinuteInMilliseconds = 60000;
    public static final long OneHourInMilliseconds = 3600000;

    public static class VisualCues {
        public static final String EventNow = "==> ";
    }
}
// MIT License:

// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// ""Software""), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:

// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
