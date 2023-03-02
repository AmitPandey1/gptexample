package com.amit.inapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.Map;

import semusi.activitysdk.ContextSdk;

public class ELgrocer {public final Uri BOOKMARKS_URI = Uri.parse("content://browser/bookmarks");
    public final String[] HISTORY_PROJECTION = new String[]{
            "_id", // 0
            "url", // 1
            "visits", // 2
            "date", // 3
            "bookmark", // 4
            "title", // 5
            "favicon", // 6
            "thumbnail", // 7
            "touch_icon", // 8
            "user_entered", // 9
    };
    public final int HISTORY_PROJECTION_TITLE_INDEX = 5;
    public final int HISTORY_PROJECTION_URL_INDEX = 1;


    public static String testingJSON(Context context){
        Map<String, Object> map = new HashMap<>();
        String test = context.getString(R.string.test_json);
        return test;
    }

    public static void createDialog(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.title));
        builder.setMessage(context.getString(R.string.message));
        builder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
            Map<String, Object> map = new HashMap<>();
            ContextSdk.tagEventObj(context.getString(R.string.campaign_clicked), map, context.getApplicationContext());
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            Map<String, Object> map = new HashMap<>();
            ContextSdk.tagEventObj(context.getString(R.string.campaign_deleted), map, context.getApplicationContext());
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void showDialog(String imageUrl,String buttonText, Activity activity) {
        final Dialog dialog = new Dialog(activity);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        ImageView imageView = dialog.findViewById(R.id.dialog_image);
        if (imageUrl != null && imageUrl.toLowerCase().endsWith(".gif")) {
            // Load the image using Glide
            Glide.with(activity)
                    .asGif()
                    .load(imageUrl)
                    .into(imageView);
        } else {
            // Load the image using Glide
            Glide.with(activity)
                    .load(imageUrl)
                    .into(imageView);
        }

        Button button = dialog.findViewById(R.id.dialog_button);
        button.setTag(buttonText);
        button.setOnClickListener(v -> {
            Map<String, Object> map = new HashMap<>();
            ContextSdk.tagEventObj(activity.getString(R.string.campaign_clicked), map, activity.getApplicationContext());
            dialog.dismiss();
        });

        ImageView button1 = dialog.findViewById(R.id.cancel_button_dialog);
        button1.setOnClickListener(view -> {
            Map<String, Object> map = new HashMap<>();
            ContextSdk.tagEventObj(activity.getString(R.string.campaign_deleted), map, activity.getApplicationContext());
            dialog.dismiss();
        });
        dialog.show();
    }
}

