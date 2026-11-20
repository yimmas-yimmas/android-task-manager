package com.playcube.taskmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** User-editable Never Close list, independent of recent-app history and retained across reboots. */
final class Protection {
    private static final String FILE = "never_close";
    private static final String KEY = "packages";
    private static final String INIT = "initialized";
    private static final Set<String> DEFAULTS = new HashSet<>(Arrays.asList(
            "com.spocky.projengmenu", "com.playcube.urlrouter"));

    private static SharedPreferences prefs(Context c) {
        SharedPreferences p = c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
        if (!p.getBoolean(INIT, false)) {
            p.edit().putStringSet(KEY, new HashSet<>(DEFAULTS)).putBoolean(INIT, true).commit();
        }
        return p;
    }

    static Set<String> saved(Context c) {
        return new HashSet<>(prefs(c).getStringSet(KEY, DEFAULTS));
    }

    static boolean contains(Context c, String pkg) {
        return saved(c).contains(pkg);
    }

    static void set(Context c, String pkg, boolean protectedNow) {
        if (essential(c, pkg)) return; // immutable safety rules
        Set<String> list = saved(c);
        if (protectedNow) list.add(pkg); else list.remove(pkg);
        prefs(c).edit().putStringSet(KEY, list).apply();
    }

    /** System/home/TV services must never be stopped, even by individual Close buttons. */
    static boolean essential(Context c, String pkg) {
        if (pkg == null || pkg.equals(c.getPackageName()) || AppList.isTvMaker(pkg)) return true;
        PackageManager pm = c.getPackageManager();
        if (AppList.homes(pm).contains(pkg)) return true;
        String ime = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        if (ime != null && pkg.equals(ime.split("/")[0])) return true;
        String a11y = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (a11y != null) for (String entry : a11y.split(":"))
            if (pkg.equals(entry.split("/")[0])) return true;
        try {
            ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
            // Do not force-stop vendor/system services or persistent apps.
            if ((info.flags & ApplicationInfo.FLAG_PERSISTENT) != 0) return true;
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                    && (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) return true;
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
        return false;
    }

    static boolean locked(Context c, String pkg) {
        return essential(c, pkg) || contains(c, pkg);
    }
}
