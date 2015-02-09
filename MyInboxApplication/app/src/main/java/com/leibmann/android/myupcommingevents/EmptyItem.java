package com.leibmann.android.myupcommingevents;

import java.io.Serializable;

/**
 * Created by matthias on 2/1/2015.
 */
public class EmptyItem implements Serializable, Item {

    public DataTypes.ItemType isItemType() {
        return DataTypes.ItemType.empty;
    }

    protected String ErrorMessage;

    public EmptyItem() {
        ErrorMessage = "";
    }
    public EmptyItem(String errorMessage) {
        ErrorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return ErrorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        ErrorMessage = errorMessage;
    }
}
