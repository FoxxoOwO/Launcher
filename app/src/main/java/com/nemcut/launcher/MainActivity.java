package com.nemcut.launcher;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.AlarmClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextClock;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Adapter adapter;
    public static List<AppInfo> appList = new ArrayList<>();
    private boolean isUserScrolling = false;
    private boolean vibrationPending = false;
    private boolean refresh = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //findViewById(R.id.searchText).setVisibility(View.GONE);


        //průhledný status bar
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );


        setClockFont();

        findViewById(R.id.settingsButton).setOnClickListener(v -> openSettings());


        //otevření budíků při kliknutí na hodiny
        findViewById(R.id.textClock).setOnClickListener(v -> {
            //Intent launchIntent = this.getPackageManager().getLaunchIntentForPackage("com.sec.android.app.clockpackage");
            //if (launchIntent != null) {
            //    this.startActivity(launchIntent);
            //}
            Intent intentAlarm = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
            if (intentAlarm.resolveActivity(getPackageManager()) != null) {
                startActivity(intentAlarm);
            }
        });

        //vibrování při dosažení začátu/konce
        recyclerView = findViewById(R.id.recyclerView);
        //NestedScrollView nsv = findViewById(R.id.nsv);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                isUserScrolling = (newState == RecyclerView.SCROLL_STATE_DRAGGING);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    vibrationPending = false; // reset flagu
                }
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy == 0 && dx == 0) return;
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();
                    int totalItems = layoutManager.getItemCount();
                    int firstVisible = layoutManager.findFirstCompletelyVisibleItemPosition();

                    LinearLayout searchLayout = findViewById(R.id.searchLayout);
                    if (searchLayout.getVisibility() == View.VISIBLE) {
                        return;
                    }

                    if ((lastVisible == totalItems - 1 || firstVisible == 0) && !vibrationPending) {
                        vibrateDevice();
                        vibrationPending = true; // blokace dalších vibrací
                    }
//                    if (lastVisible == totalItems - 1) {
//                        vibrateDevice();
//                        isUserScrolling = false;
//                    }
//
//                    if (firstVisible == 0) {
//                        vibrateDevice();
//                        isUserScrolling = false;
//                    }
                }
            }
        });

        findViewById(R.id.searchButton).setOnClickListener( v -> {
                search();
            }
        );



        // Registrace receiveru pro sledování změn aplikací
        appChangeReceiver = new AppChangeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addDataScheme("package");
        registerReceiver(appChangeReceiver, filter);

        loadApps();

        setupLayout();
    }

    private void vibrateDevice() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(10, 120));
            } else {
                vibrator.vibrate(10);
            }
        }
    }

    public void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, 100);

    }

    public void search(){
        vibrateDevice();
        //Toast.makeText(this, "Nefunguje", Toast.LENGTH_SHORT).show();
        EditText searchText = findViewById(R.id.searchText);
        LinearLayout searchLayout = findViewById(R.id.searchLayout);
        TextClock textClock = findViewById(R.id.textClock);
        ImageButton searchButton = findViewById(R.id.searchButton);
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        ImageButton closeButton = findViewById(R.id.closeButton);

        textClock.setVisibility(View.INVISIBLE);
        searchButton.setVisibility(View.INVISIBLE);
        settingsButton.setVisibility(View.INVISIBLE);

        searchLayout.setVisibility(View.VISIBLE);
        searchText.requestFocus();


        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchText, InputMethodManager.SHOW_IMPLICIT);

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        closeButton.setOnClickListener(v -> closeSearch());
    }

    private void closeSearch(){
        vibrateDevice();
        EditText searchText = findViewById(R.id.searchText);
        LinearLayout searchLayout = findViewById(R.id.searchLayout);
        TextClock textClock = findViewById(R.id.textClock);
        ImageButton searchButton = findViewById(R.id.searchButton);
        ImageButton settingsButton = findViewById(R.id.settingsButton);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);

        searchText.setText("");
        adapter.filterList("");
        searchLayout.setVisibility(View.GONE);
        textClock.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.VISIBLE);
        settingsButton.setVisibility(View.VISIBLE);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        LinearLayout searchLayout = findViewById(R.id.searchLayout);
        if (searchLayout.getVisibility() == View.VISIBLE) {
            // Pokud je aktivní hledání, zavři ho
            closeSearch();
        } else {
            //super.onBackPressed();
        }
    }



//    private void filter(String text) {
//        List<AppInfo> filteredList = new ArrayList<>();
//        for (AppInfo app : appList) {
//            if (app.label.toLowerCase().contains(text.toLowerCase())) {
//                filteredList.add(app);
//            }
//        }
//        adapter.filterList(filteredList);
//    }

    private void setupLayout() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean gridEnabled = prefs.getBoolean("grid_enabled", false);
        int gridColumns = prefs.getInt("grid_columns", 4);
        int iconSize = prefs.getInt("iconSize", 54);
        int textSize = prefs.getInt("textSize", 20);

        RecyclerView.LayoutManager layoutManager;
        if (gridEnabled) {
            layoutManager = new GridLayoutManager(this, gridColumns);
        } else {
            layoutManager = new LinearLayoutManager(this);
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(30);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        adapter = new Adapter(this, appList, gridEnabled,false,iconSize,textSize,gridColumns);
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
    }

    public void setClockFont(){
        TextClock clock = findViewById(R.id.textClock);
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        int clockFont = prefs.getInt("clockFont", 1);
        Typeface typeface = null;
        switch(clockFont) {
            case 1:
                typeface = ResourcesCompat.getFont(this,R.font.biorhyme);
                break;
            case 2:
                typeface = ResourcesCompat.getFont(this,R.font.akronim);
                break;
            case 3:
                typeface = ResourcesCompat.getFont(this,R.font.ar_one_sans);
                break;
        }
        clock.setTypeface(typeface);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (refresh) {
            setupLayout(); // nebo refreshAppList(), atd.
            refresh = false;
        }
        setClockFont();
    }

    public static Drawable resizeDrawable(Context context, Drawable drawable, int dpWidth, int dpHeight) {
        int widthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpWidth, context.getResources().getDisplayMetrics());
        int heightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpHeight, context.getResources().getDisplayMetrics());

        Bitmap bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return new BitmapDrawable(context.getResources(), bitmap);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            refresh = true;
        }
    }


    private void loadApps() {
        new Thread(() -> {
            appList.clear();
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            PackageManager pm = getPackageManager();
            List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);

            for (ResolveInfo resolveInfo : apps) {
                String label = resolveInfo.loadLabel(pm).toString();
                String packageName = resolveInfo.activityInfo.packageName;
                Drawable icon;
                try {
                    icon = resolveInfo.loadIcon(pm);
                } catch (Exception e) {
                    icon = getDrawable(R.mipmap.ic_launcher);
                }

                Drawable scaledIcon = resizeDrawable(this, icon, 60, 60);
                appList.add(new AppInfo(label, packageName, scaledIcon));
            }

            // Seřazení na pozadí
            Collections.sort(appList, (a1, a2) -> a1.label.compareToIgnoreCase(a2.label));

            // Aktualizace UI v hlavním vlákně
            runOnUiThread(() -> {
                if (adapter != null) {
                    adapter.updateList(appList);
                    adapter.notifyDataSetChanged();
                }
            });
        }).start();
    }



    private AppChangeReceiver appChangeReceiver;

    // Vnitřní třída pro sledování změn aplikací
    private class AppChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String packageName = intent.getData() != null ? intent.getData().getSchemeSpecificPart() : null;

            if (packageName == null) return;

            if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                handleAppInstalled(packageName);
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action) ||
                    Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(action)) {
                if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    handleAppUninstalled(packageName);
                }
            } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
                handleAppUpdated(packageName);
            }
        }
    }

    private void handleAppInstalled(String packageName) {
        //Log.d("AppChanges", "Instalace: " + packageName);
        // Načteme nově nainstalovanou aplikaci
        PackageManager pm = getPackageManager();
        try {
            Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                ResolveInfo resolveInfo = pm.resolveActivity(launchIntent, 0);
                String label = resolveInfo.loadLabel(pm).toString();
                Drawable icon = resolveInfo.loadIcon(pm);
                Drawable scaledIcon = resizeDrawable(this, icon, 60, 60);

                // Přidáme do seznamu
                AppInfo newApp = new AppInfo(label, packageName, scaledIcon);
                appList.add(newApp);

                // Seřadíme a aktualizujeme UI
                Collections.sort(appList, (a1, a2) -> a1.label.compareToIgnoreCase(a2.label));
                runOnUiThread(() -> adapter.updateList(appList));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleAppUninstalled(String packageName) {
        //Log.d("AppChanges", "Odinstalace: " + packageName);
        // Odstraníme aplikaci ze seznamu
        for (int i = 0; i < appList.size(); i++) {
            if (appList.get(i).packageName.equals(packageName)) {
                final int position = i;
                runOnUiThread(() -> {
                    appList.remove(position);
                    adapter.updateList(appList);
                    adapter.notifyItemRemoved(position);
                });
                break;
            }
        }

    }

    private void handleAppUpdated(String packageName) {
        //Log.d("AppChanges", "Aktualizace: " + packageName);
        // Aktualizujeme ikonu a název (pokud se změnily)
        PackageManager pm = getPackageManager();
        for (int i = 0; i < appList.size(); i++) {
            if (appList.get(i).packageName.equals(packageName)) {
                try {
                    Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
                    ResolveInfo resolveInfo = pm.resolveActivity(launchIntent, 0);
                    String newLabel = resolveInfo.loadLabel(pm).toString();
                    Drawable newIcon = resolveInfo.loadIcon(pm);
                    Drawable scaledIcon = resizeDrawable(this, newIcon, 60, 60);

                    final int position = i;
                    AppInfo updatedApp = appList.get(position);
                    updatedApp.label = newLabel;
                    updatedApp.icon = scaledIcon;

                    runOnUiThread(() -> adapter.updateList(appList));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Zrušení registrace receiveru
        if (appChangeReceiver != null) {
            unregisterReceiver(appChangeReceiver);
        }
    }



}