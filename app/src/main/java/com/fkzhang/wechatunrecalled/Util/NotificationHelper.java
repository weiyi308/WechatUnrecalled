package com.fkzhang.wechatunrecalled.Util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

import com.fkzhang.wechatunrecalled.WechatPackageNames;

import java.util.ArrayList;

import de.robv.android.xposed.XposedBridge;

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;

/**
 * Created by fkzhang on 2/6/2016.
 */
public class NotificationHelper {
    private Bitmap mNotificationLargeIcon;
    private Context mNotificationContext;
    private int mNotificationIcon;
    private WechatMainDBHelper mDb;
    private boolean mInit;
    private WechatPackageNames w;
    private SettingsHelper mSettings;
    private ArrayList<String> mUsernames;
    private Class<?> mNotificationClass;


    public NotificationHelper(WechatPackageNames packageNames) {
        w = packageNames;
        mUsernames = new ArrayList<>();
    }

    //-- comment notification ======================================================================

    public void displayNewCommentNotification(String username, String title, String content,
                                              Bitmap icon, Intent intent) {
        if (TextUtils.isEmpty(content))
            return;

        if (!mSettings.getBoolean("enable_new_comment_notification", true)) {
            return;
        }
        Bitmap largeIcon = icon == null ? getLargeIcon() : icon;

        showCommentNotification(title, content, largeIcon, username, intent);
    }

    public void showCommentNotification(String contentTitle, String contentText,
                                        Bitmap icon, String username, Intent resultIntent) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mNotificationContext)
                        .setLargeIcon(icon)
                        .setSmallIcon(mNotificationIcon)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                        .setAutoCancel(true);


//        resultIntent = new Intent();
//        resultIntent.setClassName(mNotificationContext.getPackageName(),
//                w.packageName + ".plugin.sns.ui.SnsMsgUI");

//        TaskStackBuilder stackBuilder;
//        stackBuilder = TaskStackBuilder.create(mNotificationContext);
//        stackBuilder.addParentStack(mNotificationClass);
////        stackBuilder.addParentStack(findClass(parentStack, mLoader));
//
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//        mBuilder.setContentIntent(resultPendingIntent);
//
//        XposedBridge.log(resultIntent.toString());


//        XposedBridge.log(resultIntent.toString());
//        XposedBridge.log(resultIntent.getExtras().toString());

        int notifyId = getNotificationId();


        mBuilder.setContentIntent(PendingIntent.getActivity(mNotificationContext, notifyId,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        Notification notification = mBuilder.build();

        notification.flags = Notification.FLAG_SHOW_LIGHTS
                | Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;


        notification.ledOnMS = 300;
        notification.ledOffMS = 1000;
        notification.ledARGB = Color.GREEN;

        setNotificationVibrate(notification);
        setNotificationSound(notification);

        notifyNotification(notification, notifyId);
    }

    //-- text notification =========================================================================

    public void showTextNotification(String username, String title, String content, Bitmap icon) {
        icon = icon == null ? getLargeIcon() : icon;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mNotificationContext)
                .setLargeIcon(icon)
                .setSmallIcon(mNotificationIcon)
                .setContentTitle(title)
                .setAutoCancel(true);

        if (!TextUtils.isEmpty(content)) {
            builder.setContentText(content);
        }
        showNotification(builder, getChatModeIntent(username));
    }


    public void showImageNotification(String username, String title, Bitmap bitmap, Intent resultIntent,
                                      String summary, Bitmap icon) {
        icon = icon == null ? getLargeIcon() : icon;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mNotificationContext)
                .setLargeIcon(icon)
                .setSmallIcon(mNotificationIcon)
                .setContentTitle(title)
                .setAutoCancel(true);

        if (bitmap != null) {
            builder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap).setSummaryText(summary));
        } else {
            builder.setContentText(summary);
        }

        setActionIntent(username, builder);

        resultIntent.setClassName(mNotificationContext.getPackageName(), w.ImageGalleryUI);

        showNotification(builder, resultIntent);
    }

    public void showEmojiNotification(String title, Bitmap bitmap, Intent resultIntent,
                                      String summary, Bitmap icon) {
        icon = icon == null ? getLargeIcon() : icon;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mNotificationContext)
                .setLargeIcon(icon)
                .setSmallIcon(mNotificationIcon)
                .setContentTitle(title)
                .setAutoCancel(true);

        if (bitmap != null) {
            builder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap).setSummaryText(summary));
        } else {
            builder.setContentText(summary);
        }

        resultIntent.setClassName(mNotificationContext.getPackageName(), w.ChattingUI);

        showNotification(builder, resultIntent);
    }

    protected void showNotification(NotificationCompat.Builder builder, Intent intent) {
        XposedBridge.log(intent.toString());

        int notifyId = getNotificationId();
        builder.setContentIntent(PendingIntent.getActivity(mNotificationContext, notifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_SHOW_LIGHTS
                | Notification.FLAG_AUTO_CANCEL;

        notification.ledOnMS = 300;
        notification.ledOffMS = 1000;
        notification.ledARGB = Color.GREEN;

        notifyNotification(notification, notifyId);
    }

    //-- custom notification =======================================================================

    public void displayCustomNotification(final String username, String title, String content,
                                          Bitmap icon, Bitmap bitmap, int type, Intent intent) {
        Bitmap largeIcon = icon == null ? getLargeIcon() : icon;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mNotificationContext)
                        .setLargeIcon(largeIcon)
                        .setSmallIcon(mNotificationIcon)
                        .setContentTitle(title)
                        .setNumber(mDb.getUnreadCount(username))
                        .setAutoCancel(true);

        if (!TextUtils.isEmpty(content) && bitmap == null) {
            mBuilder.setContentText(content);
        }

        if (bitmap != null) {
            mBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap).setSummaryText(content));
        }

//        Intent intent = new Intent();
        if (type == 3 || type == 62) {
            setActionIntent(username, mBuilder);
        }

        if (!mUsernames.contains(username)) {
            mUsernames.add(username);
        }

        int notifyId = mUsernames.indexOf(username);

        mBuilder.setContentIntent(PendingIntent.getActivity(mNotificationContext, notifyId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT));
        Notification notification = mBuilder.build();
//        Notification notification = buildNotification(mBuilder, intent);
//        notification.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_SHOW_LIGHTS
        notification.flags = Notification.FLAG_SHOW_LIGHTS
                | Notification.FLAG_AUTO_CANCEL;

        notification.ledOnMS = 300;
        notification.ledOffMS = 1000;
        notification.ledARGB = Color.GREEN;

        setNotificationVibrate(notification);
        setNotificationSound(notification);

        notifyNotification(notification, notifyId);
    }

    protected Notification buildNotification(NotificationCompat.Builder builder, Intent intent) {
        TaskStackBuilder stackBuilder;
        stackBuilder = TaskStackBuilder.create(mNotificationContext);
        stackBuilder.addParentStack(mNotificationClass);
//        stackBuilder.addParentStack(findClass(parentStack, mLoader));

        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        return builder.build();
    }

    private void notifyNotification(Notification notification, int id) {
        NotificationManager mNotificationManager =
                (NotificationManager) mNotificationContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, notification);
    }


    public void cancelNotifications() {
        if (!isInit())
            return;
        ((NotificationManager) mNotificationContext
                .getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
    }


    protected int getNotificationId() {
        return (int) (System.currentTimeMillis() & 0xfffffff);
    }

    public void setDB(WechatMainDBHelper db) {
        mDb = db;
    }

    public void setSettings(SettingsHelper settings) {
        mSettings = settings;
    }


    public boolean isInit() {
        return mInit;
    }

    public void init(Context context, Class<?> iconCls, Class<?> cls) {
        if (mInit || context == null || iconCls == null)
            return;

        mInit = true;

        mNotificationClass = cls;
        mNotificationContext = context;
        mNotificationIcon = (int) callStaticMethod(iconCls, w.iconMethod);
    }

    public Context getContext() {
        return mNotificationContext;
    }

    public Bitmap getLargeIcon() {
        if (mNotificationLargeIcon != null && !mNotificationLargeIcon.isRecycled()) {
            return mNotificationLargeIcon;
        }
        mNotificationLargeIcon = BitmapFactory.decodeResource(mNotificationContext
                .getResources(), mNotificationContext.getApplicationInfo().icon);
        return mNotificationLargeIcon;
    }

    private void setActionIntent(String username, NotificationCompat.Builder builder) {
        Intent action = getChatModeIntent(username);
        PendingIntent pendingIntent = PendingIntent.getActivity(mNotificationContext, 0, action, 0);
        builder.addAction(android.R.drawable.sym_action_chat, mSettings.getString("reply", "回复"), pendingIntent);
    }

    public Intent getChatModeIntent(String username) {
        Intent intent = new Intent();
        intent.setClassName(mNotificationContext.getPackageName(), w.ChattingUI);
        intent.putExtra("Chat_User", username);
        intent.putExtra("Chat_Mode", 1);
        return intent;
    }

    public Intent getImageIntent() {
        Intent intent = new Intent();
        intent.setClassName(mNotificationContext.getPackageName(), w.ImageGalleryUI);
        return intent;
    }

    protected void setNotificationSound(Notification notification, String uriString) {
        if (!TextUtils.isEmpty(uriString)) {
            notification.sound = Uri.parse(uriString);
        } else {
            notification.defaults |= Notification.DEFAULT_SOUND;
        }
    }

    protected void setNotificationVibrate(Notification notification) {
        if (mSettings.getBoolean("custom_vibrate_enable", false)) {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }
    }

    protected void setNotificationSound(Notification notification) {
        if (mSettings.getBoolean("custom_ringtone_enable", false)) {
            setNotificationSound(notification, mSettings.getString("custom_ringtone", ""));
        }
    }

}
