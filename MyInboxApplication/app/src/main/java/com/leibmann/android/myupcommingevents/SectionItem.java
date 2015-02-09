package com.leibmann.android.myupcommingevents;

import java.io.Serializable;

/**
 * Created by matthias on 1/31/2015.
 */
public class SectionItem implements Serializable, Item {

    public DataTypes.ItemType isItemType() {
        return DataTypes.ItemType.section;
    }

    protected String mUtcEventStartTime;

    public String getUtcEventStartTime() {
        return mUtcEventStartTime;
    }

    public void setUtcEventStartTime(String utcEventStartTime) {
        mUtcEventStartTime = utcEventStartTime;
    }

    public SectionItem(String utcEventStartTime) {
        mUtcEventStartTime = utcEventStartTime;
    }
}
