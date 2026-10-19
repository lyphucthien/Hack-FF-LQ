package com.lpt.lqassistant;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

public class GameMonitorService extends Service {
    private static final String MONITOR_CHANNEL = "lq_monitor_channel";
    private static final String GAME_CHANNEL = "lq_game_detected_channel";
    private static final int MONITOR_NOTIFICATION_ID = 7101;
    private static final int LQ_NOTIFICATION_ID = 7102;
    private static final int FF_NOTIFICATION_ID = 7103;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private String currentPackage = "";
    private String lastNotifiedPackage = "";
    private long lastRelevantEventTime = 0L;

    private final Runnable pollTask = new Runnable() {
        @Override public void run() {
            detectGame();
            handler.postDelayed(this, 1800L);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createChannels();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = buildMonitorNotification();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(MONITOR_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(MONITOR_NOTIFICATION_ID, notification);
        }
        getSharedPreferences("state", MODE_PRIVATE).edit().putBoolean("monitoring", true).apply();
        handler.removeCallbacks(pollTask);
        handler.post(pollTask);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(pollTask);
        getSharedPreferences("state", MODE_PRIVATE).edit().putBoolean("monitoring", false).apply();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private void detectGame() {
        UsageStatsManager manager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        if (manager == null) return;

        long now = System.currentTimeMillis();
        // Read recent events, but only apply a newest event once. This keeps the
        // last known foreground app stable between polls when no new events arrive.
        UsageEvents events;
        try {
            events = manager.queryEvents(now - 120_000L, now);
        } catch (SecurityException e) {
            return;
        }
        if (events == null) return;

        UsageEvents.Event event = new UsageEvents.Event();
        long newestTime = lastRelevantEventTime;
        String newestPackage = null;
        int newestType = -1;
        while (events.hasNextEvent()) {
            events.getNextEvent(event);
            int type = event.getEventType();
            if (!isForegroundEvent(type) && !isBackgroundEvent(type)) continue;
            long timestamp = event.getTimeStamp();
            if (timestamp > newestTime) {
                newestTime = timestamp;
                newestPackage = event.getPackageName();
                newestType = type;
            }
        }

        if (newestPackage != null) {
            lastRelevantEventTime = newestTime;
            if (isForegroundEvent(newestType)) {
                currentPackage = newestPackage;
            } else if (newestPackage.equals(currentPackage)) {
                currentPackage = "";
            }
        }

        String currentGame = gameIdForPackage(currentPackage);
        if (currentGame == null) {
            lastNotifiedPackage = "";
            return;
        }
        if (!currentPackage.equals(lastNotifiedPackage)) {
            showGameNotification(currentGame, currentPackage);
            lastNotifiedPackage = currentPackage;
        }
    }

    private boolean isForegroundEvent(int type) {
        if (type == UsageEvents.Event.MOVE_TO_FOREGROUND) return true;
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && type == UsageEvents.Event.ACTIVITY_RESUMED;
    }

    private boolean isBackgroundEvent(int type) {
        if (type == UsageEvents.Event.MOVE_TO_BACKGROUND) return true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return type == UsageEvents.Event.ACTIVITY_PAUSED || type == UsageEvents.Event.ACTIVITY_STOPPED;
        }
        return false;
    }

    private String gameIdForPackage(String packageName) {
        if (packageName == null) return null;
        if (packageName.equals("com.garena.game.kgvn")) return "lq";
        if (packageName.equals("com.dts.freefireth") || packageName.equals("com.dts.freefiremax")) return "ff";
        return null;
    }

    private void createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm == null) return;
        nm.createNotificationChannel(new NotificationChannel(MONITOR_CHANNEL, "Trạng thái nhận diện", NotificationManager.IMPORTANCE_LOW));
        nm.createNotificationChannel(new NotificationChannel(GAME_CHANNEL, "Thông báo game được nhận diện", NotificationManager.IMPORTANCE_DEFAULT));
    }

    private Notification buildMonitorNotification() {
        Intent open = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(this, 7101, open, PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag());
        Notification.Builder b = builder(MONITOR_CHANNEL)
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentTitle("LQ Assistant đang nhận diện")
                .setContentText("Mở Liên Quân hoặc Free Fire để nhận thông báo hướng dẫn.")
                .setContentIntent(pending)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setOnlyAlertOnce(true);
        return b.build();
    }

    private void showGameNotification(String game, String packageName) {
        boolean lq = "lq".equals(game);
        String title = lq ? "Đã nhận diện Liên Quân Mobile" : "Đã nhận diện Free Fire";
        String body = lq ? "Chạm để mở hướng dẫn Liên Quân." : "Chạm để mở hướng dẫn Free Fire.";
        Intent open = new Intent(this, MainActivity.class)
                .putExtra("selectedGame", game)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int id = lq ? LQ_NOTIFICATION_ID : FF_NOTIFICATION_ID;
        PendingIntent pending = PendingIntent.getActivity(this, id, open, PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag());
        Notification notification = builder(GAME_CHANNEL)
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pending)
                .setAutoCancel(true)
                .setCategory(Notification.CATEGORY_RECOMMENDATION)
                .setOnlyAlertOnce(true)
                .build();
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(id, notification);
    }

    private Notification.Builder builder(String channel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) return new Notification.Builder(this, channel);
        return new Notification.Builder(this);
    }

    private int immutableFlag() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;
    }
}
