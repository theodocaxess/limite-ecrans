package com.theo.limitesolal;

import android.Manifest;
import android.content.Intent;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

@CapacitorPlugin(
    name = "ScreenLimit",
    permissions = {
        @Permission(strings = { Manifest.permission.POST_NOTIFICATIONS }, alias = "notifications")
    }
)
public class ScreenLimitPlugin extends Plugin {

    @PluginMethod
    public void startMonitoring(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && getPermissionState("notifications") != PermissionState.GRANTED) {
            requestPermissionForAlias("notifications", call, "notificationsPermsCallback");
            return;
        }
        doStart(call);
    }

    @PermissionCallback
    private void notificationsPermsCallback(PluginCall call) {
        doStart(call);
    }

    private void doStart(PluginCall call) {
        PeriodSchedule.setMonitoringEnabled(getContext(), true);
        Intent intent = new Intent(getContext(), ScreenLimitService.class);
        ContextCompat.startForegroundService(getContext(), intent);

        JSObject ret = new JSObject();
        ret.put("running", true);
        call.resolve(ret);
    }

    @PluginMethod
    public void stopMonitoring(PluginCall call) {
        PeriodSchedule.setMonitoringEnabled(getContext(), false);
        Intent intent = new Intent(getContext(), ScreenLimitService.class);
        getContext().stopService(intent);

        JSObject ret = new JSObject();
        ret.put("running", false);
        call.resolve(ret);
    }

    @PluginMethod
    public void getStatus(PluginCall call) {
        boolean enabled = PeriodSchedule.isMonitoringEnabled(getContext());
        boolean granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            || getPermissionState("notifications") == PermissionState.GRANTED;

        JSObject ret = new JSObject();
        ret.put("enabled", enabled);
        ret.put("notificationsGranted", granted);
        call.resolve(ret);
    }
}
