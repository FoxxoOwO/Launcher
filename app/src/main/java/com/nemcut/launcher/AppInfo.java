package com.nemcut.launcher;

import android.graphics.drawable.Drawable;

public class AppInfo {
    public String label;
    public String packageName;
    public Drawable icon;

    public AppInfo(String label, String packageName, Drawable icon) {
        this.label = label;
        this.packageName = packageName;
        this.icon = icon;
    }


}
