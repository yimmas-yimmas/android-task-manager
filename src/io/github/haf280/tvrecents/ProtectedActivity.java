package com.playcube.taskmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** D-pad friendly protection manager. Preferences are NOT cleared on boot. */
public class ProtectedActivity extends Activity {
    static final int BG = 0xFF0B0B0D, CARD = 0xFF17171B, FOCUS = 0xFF24242A;
    final Typeface medium = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    LinearLayout list;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BG);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(0, dp(36), 0, dp(24));
        root.addView(column, new FrameLayout.LayoutParams(dp(800), -1, Gravity.CENTER_HORIZONTAL));
        TextView title = label("Never Close", 27, 0xFFFFFFFF);
        title.setTypeface(medium);
        column.addView(title);
        TextView hint = label("Shield ON = protected from ×, Close Selected and Close All.\nProjectivy and URL Router start protected. HOME and system apps are always protected.", 14, 0xB3FFFFFF);
        hint.setPadding(0, dp(10), 0, dp(18));
        column.addView(hint);
        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        column.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        populate();
    }

    void populate() {
        list.removeAllViews();
        PackageManager pm = getPackageManager();
        List<String[]> apps = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        // Include recent user-visible apps and custom apps without requiring QUERY_ALL_PACKAGES.
        for (String category : new String[] {Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER}) {
            Intent q = new Intent(Intent.ACTION_MAIN).addCategory(category);
            for (ResolveInfo r : pm.queryIntentActivities(q, 0)) {
                String pkg = r.activityInfo.packageName;
                if (seen.add(pkg) && !Protection.essential(this, pkg)) {
                    apps.add(new String[] {pkg, r.activityInfo.loadLabel(pm).toString()});
                }
            }
        }
        // Include saved entries, even if their apps are not launchable.
        for (String pkg : Protection.saved(this)) {
            if (!seen.add(pkg) || Protection.essential(this, pkg)) continue;
            try {
                apps.add(new String[] {pkg, pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()});
            } catch (PackageManager.NameNotFoundException ignored) { }
        }
        Collections.sort(apps, (x, y) -> x[1].compareToIgnoreCase(y[1]));
        View first = null;
        for (String[] app : apps) {
            final String pkg = app[0];
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(20), dp(13), dp(20), dp(13));
            row.setFocusable(true);
            row.setClickable(true);
            TextView mark = label("", 17, 0xFFFFFFFF);
            mark.setTypeface(medium);
            mark.setMinWidth(dp(118));
            row.addView(mark);
            LinearLayout texts = new LinearLayout(this);
            texts.setOrientation(LinearLayout.VERTICAL);
            TextView title = label(app[1], 17, 0xFFFFFFFF);
            texts.addView(title);
            texts.addView(label(pkg, 11, 0x80FFFFFF));
            row.addView(texts);
            Runnable refresh = () -> mark.setText(Protection.contains(this, pkg) ? "● ON" : "○ OFF");
            refresh.run();
            row.setOnClickListener(v -> {
                Protection.set(this, pkg, !Protection.contains(this, pkg));
                refresh.run();
            });
            row.setOnFocusChangeListener((v, focused) -> {
                GradientDrawable g = new GradientDrawable();
                g.setCornerRadius(dp(10));
                g.setColor(focused ? FOCUS : CARD);
                if (focused) g.setStroke(dp(2), 0xFFFFFFFF);
                row.setBackground(g);
            });
            GradientDrawable g = new GradientDrawable();
            g.setColor(CARD);
            g.setCornerRadius(dp(10));
            row.setBackground(g);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.bottomMargin = dp(7);
            list.addView(row, lp);
            if (first == null) first = row;
        }
        if (first != null) first.requestFocus();
    }

    TextView label(String t, int sp, int color) {
        TextView v = new TextView(this);
        v.setText(t); v.setTextSize(sp); v.setTextColor(color); return v;
    }
    int dp(int x) { return Math.round(x * getResources().getDisplayMetrics().density); }
}
