package com.nemcut.launcher;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class Launcher extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Aplikuj dynamické barvy hned při startu aplikace
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
