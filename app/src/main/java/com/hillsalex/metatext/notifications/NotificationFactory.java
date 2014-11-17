package com.hillsalex.metatext.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.hillsalex.metatext.R;
import com.hillsalex.metatext.ThreadDetailViewActivity;
import com.hillsalex.metatext.ThreadListViewActivity;
import com.hillsalex.metatext.model.MessageModel;
import com.hillsalex.metatext.model.MmsMessageModel;
import com.hillsalex.metatext.model.SmsMessageModel;
import com.hillsalex.metatext.util.Util;

import java.util.Random;

/**
 * Created by alex on 11/11/2014.
 */


public class NotificationFactory {

    private static Random mRandom;

    static int receivedMessageLogo = R.drawable.logo_transparent_white_128;
    static int receivedMessageLogoDark = R.drawable.logo_transparent_black_128;

    public static void makeNotification(Context context, String title, String message) {
        if (mRandom == null) mRandom = new Random();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.message_icon)
                .setContentTitle(title)
                .setContentText(message);

        Intent resultIntent = new Intent(context, ThreadListViewActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(ThreadListViewActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(mRandom.nextInt(), builder.build());
    }

    private static Bitmap getLargeIcon(Resources res, Drawable d) {
        int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
        int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
        return Util.scaleCenterCrop(Util.drawableToBitmap(d), height, width);
    }

    private static Bitmap getWearableBackground(Resources res, Drawable d) {
        if (true) return Util.drawableToBitmap(d);
        int height = 400;
        int width = 600;
        return Util.scaleCenterCrop(Util.drawableToBitmap(d), height, width);
    }

    private static NotificationCompat.Builder makeBuilderFor(Context context, int smallIcon, Bitmap largeIcon, String title, String message) {
        return new NotificationCompat.Builder(context)
                .setSmallIcon(smallIcon)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(message);
    }

    private static NotificationCompat.Builder makeBuilderFor(
            Context context, int smallIcon, Bitmap largeIcon, String title, String message,
            int wearableIcon, Bitmap background) {
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setContentIcon(wearableIcon)
                .setHintHideIcon(true)
                .setContentIntentAvailableOffline(false)
                .setBackground(background);

        return new NotificationCompat.Builder(context)
                .setSmallIcon(smallIcon)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(message)
                .extend(wearableExtender);

    }

    private static PendingIntent getStackBuilderPendingIntentFor(Context context, Intent resultIntent) {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(ThreadListViewActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        return stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    public static void makeNotificationError(Context context, MessageModel model, Throwable e) {
        Intent resultIntent = new Intent(context, ThreadListViewActivity.class);

        String error = "Contact was " + (model!=null ? " not null " : "null") + "." + (model==null ? "" : model.senderContact);
        NotificationCompat.Builder builder = makeBuilderFor(
                context, receivedMessageLogo, getLargeIcon(context.getResources(), context.getResources().getDrawable(receivedMessageLogo)), "Error with sms retrieval",error
                );

        Log.e("Notifications", e.getLocalizedMessage(), e);
        e.printStackTrace();

        builder.setVibrate(new long[]{0, 500});
        builder.setContentIntent(getStackBuilderPendingIntentFor(context, resultIntent));

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) model.threadId, builder.build());
    }

    public static void makeNotificationForSms(Context context, SmsMessageModel model) {
        if (model == null || (model.senderContact==null && model.fromMe==false)) return;
        try {
            String title = model.senderContact.getName();
            String message = model.body;
            Resources res = context.getResources();
            Drawable contactAvatar = model.senderContact.getAvatar(context, res.getDrawable(R.drawable.logo));

            Intent resultIntent = new Intent(context, ThreadDetailViewActivity.class);
            resultIntent.putExtra("cameFromNotification", true);
            resultIntent.putExtra("threadId", model.threadId);


            NotificationCompat.Builder builder = makeBuilderFor(
                    context, receivedMessageLogo, getLargeIcon(res, contactAvatar), title, message,
                    receivedMessageLogoDark, getWearableBackground(res, contactAvatar));
            builder.setVibrate(new long[]{0, 500});
            builder.setContentIntent(getStackBuilderPendingIntentFor(context, resultIntent));

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify((int) model.threadId, builder.build());
        } catch (Exception e) {
            makeNotificationError(context, model, e);
        }
    }

    public static void makeNotificationForMms(Context context, MmsMessageModel model) {
        try {
            String title = model.senderContact.getName();
            String message = model.body;
            if ((message.equals("") || message == null) && model.mImageUris.size() > 0) {
                message = "Image received";
            }
            if ((message.equals("") || message == null))
                message = "MMS received";

            Resources res = context.getResources();
            Drawable contactAvatar = model.senderContact.getAvatar(context, res.getDrawable(R.drawable.logo));


            Intent resultIntent = new Intent(context, ThreadDetailViewActivity.class);
            resultIntent.putExtra("cameFromNotification", true);
            resultIntent.putExtra("threadId", model.threadId);


            NotificationCompat.Builder builder = makeBuilderFor(
                    context, receivedMessageLogo, getLargeIcon(res, contactAvatar), title, message,
                    receivedMessageLogoDark, getWearableBackground(res, contactAvatar));
            builder.setVibrate(new long[]{0, 500});
            builder.setContentIntent(getStackBuilderPendingIntentFor(context, resultIntent));

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify((int) model.threadId, builder.build());
        } catch (Exception e) {
            makeNotificationError(context, model, e);
        }
    }
}