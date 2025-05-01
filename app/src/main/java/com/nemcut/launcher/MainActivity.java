package com.nemcut.launcher;

import static java.security.AccessController.getContext;
import static java.util.Locale.filter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.AlarmClock;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.DynamicColors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Adapter adapter;
    public static List<AppInfo> appList = new ArrayList<>();

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



        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        PackageManager pm = this.getPackageManager();
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


            Drawable scaledIcon = resizeDrawable(this, icon, 60, 60);  // 54 dp na px
            appList.add(new AppInfo(label, packageName, scaledIcon));
            //appList.add(new AppInfo(label, packageName, icon));
        }

        //seřazení listu podle abecedy
        appList.sort(new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo a1, AppInfo a2) {
                return a1.label.compareToIgnoreCase(a2.label);
            }
        });


        setupLayout();


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
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();
                    int totalItems = layoutManager.getItemCount();
                    int firstVisible = layoutManager.findFirstCompletelyVisibleItemPosition();

                    LinearLayout searchLayout = findViewById(R.id.searchLayout);
                    if (searchLayout.getVisibility() == View.VISIBLE) {
                        return;
                    }


                    if (lastVisible == totalItems - 1) {
                        vibrateDevice();
                    }

                    if (firstVisible == 0) {
                        vibrateDevice();
                    }
                }
            }
        });

        findViewById(R.id.searchButton).setOnClickListener( v -> {
                search();
            }
        );


    }

    private void vibrateDevice() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(30);
            }
        }
    }

    public void openSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void search(){
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
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        closeButton.setOnClickListener(v -> closeSearch());
    }

    private void closeSearch(){
        EditText searchText = findViewById(R.id.searchText);
        LinearLayout searchLayout = findViewById(R.id.searchLayout);
        TextClock textClock = findViewById(R.id.textClock);
        ImageButton searchButton = findViewById(R.id.searchButton);
        ImageButton settingsButton = findViewById(R.id.settingsButton);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);

        searchText.setText("");
        searchLayout.setVisibility(View.GONE);
        textClock.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.VISIBLE);
        settingsButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        LinearLayout searchLayout = findViewById(R.id.searchLayout);
        if (searchLayout.getVisibility() == View.VISIBLE) {
            // Pokud je aktivní hledání, zavři ho
            closeSearch();
        } else {
            // Jinak běž normálně zpět
            //super.onBackPressed();
        }
    }

    private void filter(String text) {
        List<AppInfo> filteredList = new ArrayList<>();
        for (AppInfo app : appList) {
            if (app.label.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(app);
            }
        }
        adapter.filterList(filteredList);
    }

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
        setupLayout();
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




}