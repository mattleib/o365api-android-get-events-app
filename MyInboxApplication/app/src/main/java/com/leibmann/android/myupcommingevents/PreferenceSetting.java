package com.leibmann.android.myupcommingevents;

import java.io.Serializable;

/**
 * Created by matthias on 1/31/2015.
 */
public class PreferenceSetting implements Serializable {

    private String oldValue;
    private String newValue;

    public Boolean getHasChanged() {
        return !(oldValue.equals(newValue));
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public PreferenceSetting(String oldValue, String newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
}
