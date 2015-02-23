//Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
//
package com.leibmann.android.myupcommingevents;

import java.io.Serializable;

/**
 * Created by matthias on 2/1/2015.
 */
public class EmptyItem implements Serializable, Item {

    private static final long serialVersionUID = 0L;

    public ItemType isItemType() {
        return ItemType.Empty;
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
