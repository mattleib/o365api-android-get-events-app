package com.leibmann.android.myupcommingevents;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by mattleib on 2/9/2015.
 */
public class EventsCache {

    private static final String TAG = "EventsCache";
    private static final String FileName = "events_file";

    public static Boolean write(ArrayList<Item> items, Context context)
    {
        Log.d(TAG, Helpers.LogEnterMethod("Write"));

        try {
            FileOutputStream fos = context.openFileOutput(FileName, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(items);
            os.close();
            fos.close();

            Log.d(TAG, Helpers.LogLeaveMethod("Write"));
            return true;
        }
        catch(Exception e) {

            Log.d(TAG, Helpers.LogLeaveMethod("Write") + "::Exception", e);
            return false;
        }
    }

    public static ArrayList<Item> read(Context context)
    {
        Log.d(TAG, Helpers.LogEnterMethod("Read"));

        try {
            FileInputStream fis = context.openFileInput(FileName);
            ObjectInputStream is = new ObjectInputStream(fis);
            ArrayList<Item> items = (ArrayList<Item>) is.readObject();
            is.close();
            fis.close();

            Log.d(TAG, Helpers.LogLeaveMethod("Read"));
            return items;
        }
        catch(Exception e) {

            Log.d(TAG, Helpers.LogLeaveMethod("Read") + "::Exception", e);
            return null;
        }
    }
}
