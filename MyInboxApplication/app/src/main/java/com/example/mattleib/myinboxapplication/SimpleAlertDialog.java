package com.example.mattleib.myinboxapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by mattleib on 1/27/2015.
 */
public class SimpleAlertDialog {
    /**
     * show simple Alert Dialog
     * */
    public static void showAlertDialog(Context context, String title, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title)
                .setMessage(message)
                .setNegativeButton("Close",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        // Setting OK Button
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        // Showing Alert Message
        AlertDialog alert = dialog.create();
        alert.show();
    }
}
