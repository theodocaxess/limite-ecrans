package com.theo.limitesolal;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.util.Calendar;

/**
 * Definition des creneaux et lecture de l'etat coche/decoche
 * enregistre par la grille (partage la meme SharedPreferences que le
 * plugin @capacitor/preferences : fichier "CapacitorStorage").
 */
public class PeriodSchedule {

    public static final String PREFS_FILE = "CapacitorStorage";
    public static final String STATE_KEY = "screenLimitState_v1";
    public static final String MONITORING_KEY = "monitoringEnabled";

    // Un "creneau" = identifiant + heure de debut + heure de fin + jours ou 1=Lundi ... 7=Dimanche
    private static class Period {
        final String id;
        final int startHour, startMinute, endHour, endMinute;
        final int[] days;

        Period(String id, int startHour, int startMinute, int endHour, int endMinute, int[] days) {
            this.id = id;
            this.startHour = startHour;
            this.startMinute = startMinute;
            this.endHour = endHour;
            this.endMinute = endMinute;
            this.days = days;
        }
    }

    private static final Period[] PERIODS = new Period[] {
        new Period("morning", 7, 30, 8, 0, new int[] {1, 2, 3, 4, 5}),
        new Period("evening", 18, 0, 20, 0, new int[] {1, 2, 3, 4, 5, 6, 7}),
        new Period("wednesday", 13, 0, 20, 0, new int[] {3}),
        new Period("weekend", 7, 30, 20, 0, new int[] {6, 7}),
    };

    /** Renvoie true si l'instant present tombe dans un creneau actuellement coche par l'utilisateur. */
    public static boolean isWithinActivePeriod(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        String json = prefs.getString(STATE_KEY, null);

        Calendar now = Calendar.getInstance();
        int calendarDay = now.get(Calendar.DAY_OF_WEEK); // 1=Dimanche ... 7=Samedi
        int ourDay = (calendarDay == Calendar.SUNDAY) ? 7 : calendarDay - 1; // 1=Lundi ... 7=Dimanche
        int nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);

        JSONObject state = null;
        if (json != null) {
            try {
                state = new JSONObject(json);
            } catch (Exception ignored) {
            }
        }

        for (Period period : PERIODS) {
            boolean appliesToday = false;
            for (int d : period.days) {
                if (d == ourDay) {
                    appliesToday = true;
                    break;
                }
            }
            if (!appliesToday) continue;

            int startMinutes = period.startHour * 60 + period.startMinute;
            int endMinutes = period.endHour * 60 + period.endMinute;
            if (nowMinutes < startMinutes || nowMinutes >= endMinutes) continue;

            String key = period.id + "_" + ourDay;
            boolean checked = true; // actif par defaut si aucun reglage sauvegarde
            if (state != null && state.has(key)) {
                checked = state.optBoolean(key, true);
            }
            if (checked) return true;
        }
        return false;
    }

    public static boolean isMonitoringEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        return "true".equals(prefs.getString(MONITORING_KEY, "false"));
    }

    public static void setMonitoringEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        prefs.edit().putString(MONITORING_KEY, enabled ? "true" : "false").apply();
    }
}
