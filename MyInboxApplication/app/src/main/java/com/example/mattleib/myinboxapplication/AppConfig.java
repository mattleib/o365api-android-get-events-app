package com.example.mattleib.myinboxapplication;

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

    public String getAuthority() {
        return Authority;
    }

    public void setAuthority(String authority) {
        Authority = authority;
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

    public AppConfig(String authority, String clientId, String redirectUri, String resourceExchange, String userHint, String eventsQueryTemplate) {
        Authority = authority;
        ClientId = clientId;
        RedirectUri = redirectUri;
        ResourceExchange = resourceExchange;
        UserHint = userHint;
        EventsQueryTemplate = eventsQueryTemplate;
    }
}
