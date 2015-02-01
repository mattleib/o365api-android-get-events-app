package com.example.mattleib.myinboxapplication;

import java.io.Serializable;

/**
 * Created by matthias on 2/1/2015.
 */
public class EmptyItem implements Serializable, Item {

    public DataTypes.ItemType isItemType() {
        return DataTypes.ItemType.empty;
    }

}
