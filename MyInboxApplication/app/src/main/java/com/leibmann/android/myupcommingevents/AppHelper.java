package com.leibmann.android.myupcommingevents;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by mattleib on 1/26/2015.
 */
public class AppHelper {
    public static void close(Closeable obj){
        if(obj!=null){
            try {
                obj.close();
            }catch (IOException e){
                //ignore
            }
        }
    }
}
