package com.theo.limitesolal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

/**
 * Relance le service de surveillance apres un redemarrage du telephone,
 * si la surveillance etait activee avant l'extinction.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (PeriodSchedule.isMonitoringEnabled(context)) {
                Intent serviceIntent = new Intent(context, ScreenLimitService.class);
                ContextCompat.startForegroundService(context, serviceIntent);
            }
        }
    }
}
