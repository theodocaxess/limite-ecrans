package com.theo.limitesolal;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

/**
 * Service de premier plan qui ecoute le deverrouillage de l'ecran (ACTION_USER_PRESENT)
 * et affiche une notification en premier plan (heads-up) si l'instant present tombe
 * dans un creneau actuellement coche par l'utilisateur.
 *
 * Android exige qu'un service de premier plan garde une notification permanente
 * et visible tant qu'il tourne : c'est la notification "Surveillance active" ci-dessous.
 */
public class ScreenLimitService extends Service {

    private static final String MONITORING_CHANNEL_ID = "screen_limit_monitoring";
    private static final String ALERT_CHANNEL_ID = "screen_limit_alert";
    private static final int MONITORING_NOTIF_ID = 1;
    private static final int ALERT_NOTIF_ID = 2;

    private BroadcastReceiver unlockReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannels();

        unlockReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                    if (PeriodSchedule.isWithinActivePeriod(context)) {
                        showAlert();
                    }
                }
            }
        };
        registerReceiver(unlockReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(MONITORING_NOTIF_ID, buildMonitoringNotification());
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(unlockReceiver);
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = getSystemService(NotificationManager.class);

        NotificationChannel monitoring = new NotificationChannel(
            MONITORING_CHANNEL_ID,
            "Surveillance active",
            NotificationManager.IMPORTANCE_MIN
        );
        monitoring.setDescription("Indique que la surveillance des creneaux ecrans est active.");
        monitoring.setShowBadge(false);
        manager.createNotificationChannel(monitoring);

        NotificationChannel alert = new NotificationChannel(
            ALERT_CHANNEL_ID,
            "Rappel limite ecrans",
            NotificationManager.IMPORTANCE_HIGH
        );
        alert.setDescription("Rappel affiche a chaque deverrouillage pendant un creneau actif.");
        manager.createNotificationChannel(alert);
    }

    private Notification buildMonitoringNotification() {
        Intent openApp = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent contentIntent = PendingIntent.getActivity(
            this, 0, openApp,
            PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, MONITORING_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Limite ecrans : surveillance active")
            .setContentText("Un rappel s'affichera au deverrouillage pendant les creneaux actifs.")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .build();
    }

    private void showAlert() {
        Intent openApp = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent contentIntent = PendingIntent.getActivity(
            this, 1, openApp,
            PendingIntent.FLAG_IMMUTABLE
        );

        Notification alert = new NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Limite ecrans - Solal")
            .setContentText("Attention les ecrans devant Solal, on LIMITE AU MAX !")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build();

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(ALERT_NOTIF_ID, alert);
    }
}
