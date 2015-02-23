//Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
//
package com.leibmann.android.myupcommingevents;

/**
 * Created by matthias on 1/31/2015.
 */
public class AppConfig {
    private String Authority;
    private String ClientId;
    private String RedirectUri;
    private String ResourceExchange;
    private String UserHint;
    private String EventsQueryTemplate;
    private String SendEmailUri;

    public String getAuthority() {
        return Authority;
    }

    public void setAuthority(String authority) {
        Authority = authority;
    }

    public String getSendEmailUri() {
        return SendEmailUri;
    }

    public void setSendEmailUri(String sendEmailUri) {
        SendEmailUri = sendEmailUri;
    }

    public String getClientId() {
        return ClientId;
    }

    public void setClientId(String clientId) {
        ClientId = clientId;
    }

    public String getRedirectUri() {
        return RedirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        RedirectUri = redirectUri;
    }

    public String getResourceExchange() {
        return ResourceExchange;
    }

    public void setResourceExchange(String resourceExchange) {
        ResourceExchange = resourceExchange;
    }

    public String getUserHint() {
        return UserHint;
    }

    public void setUserHint(String userHint) {
        UserHint = userHint;
    }

    public String getEventsQueryTemplate() {
        return EventsQueryTemplate;
    }

    public void setEventsQueryTemplate(String eventsQueryTemplate) {
        EventsQueryTemplate = eventsQueryTemplate;
    }

    public AppConfig(String authority, String clientId, String redirectUri, String resourceExchange, String userHint, String eventsQueryTemplate, String sendEmailUri) {
        Authority = authority;
        ClientId = clientId;
        RedirectUri = redirectUri;
        ResourceExchange = resourceExchange;
        UserHint = userHint;
        EventsQueryTemplate = eventsQueryTemplate;
        SendEmailUri = sendEmailUri;
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