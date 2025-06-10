package com.nemcut.launcher;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.MenuItem;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.slider.Slider;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private MaterialSwitch gridSwitch;
    private Slider gridWidth;

    private Slider iconSizeSlider;
    private Slider textSizeSlider;
    private TextView widthLabel;
    private Button fontButton1;
    private Button fontButton2;
    private Button fontButton3;
    private TextClock clock;
    private boolean gridEnabled;
    private int gridColumns;
    private int clockFont;
    private int iconSize;
    private int textSize;
    private Button defaultB;
    private boolean whiteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        gridSwitch = findViewById(R.id.gridSwitch);
        gridWidth = findViewById(R.id.gridWidth);
        widthLabel = findViewById(R.id.widthLabel);
        fontButton1 = findViewById(R.id.fontButton1);
        fontButton2 = findViewById(R.id.fontButton2);
        fontButton3 = findViewById(R.id.fontButton3);
        clock = findViewById(R.id.clock);
        iconSizeSlider = findViewById(R.id.iconSizeSlider);
        textSizeSlider = findViewById(R.id.textSizeSlider);
        defaultB =  findViewById(R.id.defaultButton);
        RadioButton radioWhite = findViewById(R.id.radioWhite);
        RadioButton radioBlack = findViewById(R.id.radioBlack);

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        gridEnabled = prefs.getBoolean("grid_enabled", false);
        gridColumns = prefs.getInt("grid_columns", 4);
        clockFont = prefs.getInt("clockFont", 3);
        iconSize = prefs.getInt("iconSize", 54);
        textSize = prefs.getInt("textSize", 20);
        whiteText = prefs.getBoolean("white_text", true);



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


        fontButton1.setOnClickListener(v->{
            var tf = ResourcesCompat.getFont(this,R.font.biorhyme);
            animateClockFontChange(tf);
            prefs.edit().putInt("clockFont", 1).apply();
        });

        fontButton2.setOnClickListener(v->{
            var tf = ResourcesCompat.getFont(this,R.font.akronim);
            animateClockFontChange(tf);
            prefs.edit().putInt("clockFont", 2).apply();
        });

        fontButton3.setOnClickListener(v->{
            var tf = ResourcesCompat.getFont(this,R.font.ar_one_sans);
            animateClockFontChange(tf);
            prefs.edit().putInt("clockFont", 3).apply();
        });

        gridSwitch.setChecked(gridEnabled);
        gridWidth.setValue(gridColumns);
        widthLabel.setText(String.valueOf(gridColumns));
        iconSizeSlider.setValue(iconSize);
        textSizeSlider.setValue(textSize);

        if (whiteText) {
            radioWhite.setChecked(true);
        } else {
            radioBlack.setChecked(true);
        }



        getSupportActionBar().setTitle(R.string.settings); // nebo jiný text

        MaterialSwitch grid = findViewById(R.id.gridSwitch);

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);

        LinearLayout animatedContainer = findViewById(R.id.animatedContainer);
        AutoTransition transition = new AutoTransition();
        transition.setDuration(220);
        transition.setInterpolator(new AccelerateDecelerateInterpolator());
        setupLayout();

        radioWhite.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                prefs.edit().putBoolean("white_text", isChecked).apply();
                whiteText = true;
                setupLayout();
            } else {
                prefs.edit().putBoolean("white_text", isChecked).apply();
                whiteText = false;
                setupLayout();
            }
        });

        gridSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("grid_enabled", isChecked).apply();
            gridEnabled = isChecked;
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK));
                } else {
                    vibrator.vibrate(10);
                }
            }
//            fadeVisibility(findViewById(R.id.gridWidthText), isChecked);
//            fadeVisibility(findViewById(R.id.gridWidthLayout), isChecked);
//            fadeVisibility(findViewById(R.id.gridWidthSpace), isChecked);
            TransitionManager.beginDelayedTransition(animatedContainer, transition);
            if (isChecked) {
                findViewById(R.id.gridWidthText).setVisibility(VISIBLE);
                findViewById(R.id.gridWidthLayout).setVisibility(VISIBLE);
                findViewById(R.id.gridWidthSpace).setVisibility(VISIBLE);
            } else {
                findViewById(R.id.gridWidthText).setVisibility(GONE);
                findViewById(R.id.gridWidthLayout).setVisibility(GONE);
                findViewById(R.id.gridWidthSpace).setVisibility(GONE);
            }
            setupLayout();
        });

        gridWidth.addOnChangeListener((slider, value, fromUser) -> {
            widthLabel.setText(String.valueOf((int) value));
            prefs.edit().putInt("grid_columns", (int) value).apply();
            gridColumns = (int) value;
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK));
                } else {
                    vibrator.vibrate(10);
                }
            }
            setupLayout();
        });

        iconSizeSlider.addOnChangeListener((slider, value, fromUser) -> {
            prefs.edit().putInt("iconSize", (int) value).apply();
            iconSize = (int) value;
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createOneShot(3, 3));
                } else {
                    vibrator.vibrate(5);
                }
            }
            setupLayout();
        });

        textSizeSlider.addOnChangeListener((slider, value, fromUser) -> {
            prefs.edit().putInt("textSize", (int) value).apply();
            textSize = (int) value;
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createOneShot(3, 3));
                } else {
                    vibrator.vibrate(5);
                }
            }
            setupLayout();
        });


        defaultB.setOnClickListener(v -> {
            Intent intent = new Intent(android.provider.Settings.ACTION_HOME_SETTINGS);
            startActivity(intent);
        });


        if (isDefaultLauncher()) {
            defaultB.setText(R.string.default_launcher_true);
        } else {
            defaultB.setText(R.string.default_launcher_false);
        }

        if (!gridEnabled) {
            findViewById(R.id.gridWidthText).setVisibility(GONE);
            findViewById(R.id.gridWidthLayout).setVisibility(GONE);
            findViewById(R.id.gridWidthSpace).setVisibility(GONE);
        }








    }

    // this event will enable the back
    // function to the button on press
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupLayout() {
        RecyclerView previewRecycler = findViewById(R.id.icon_preview_recycler);
        RecyclerView.LayoutManager layoutManager;
        List<AppInfo> previewList;
        if (gridEnabled) {
            layoutManager = new GridLayoutManager(this, gridColumns);
            previewList = MainActivity.appList.subList(0, Math.min(MainActivity.appList.size(), gridColumns));
        } else {
            layoutManager = new LinearLayoutManager(this);
            previewList = MainActivity.appList.subList(0, Math.min(MainActivity.appList.size(), 3));
        }

        previewRecycler.setLayoutManager(layoutManager);
        // Vyber třeba 5 ikon z appList
        //List<AppInfo> previewList = MainActivity.appList.subList(0, Math.min(MainActivity.appList.size(), gridColumns));

        // Vytvoř nový adapter (nebo uprav stávající, aby měl režim náhledu)
        Adapter previewAdapter = new Adapter(this, previewList, gridEnabled,true, iconSize, textSize,gridColumns, whiteText);
        previewRecycler.setAdapter(previewAdapter);
    }

    public boolean isDefaultLauncher() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setPackage(null);

        ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo == null) return false;

        String currentLauncherPackage = resolveInfo.activityInfo.packageName;
        return getPackageName().equals(currentLauncherPackage);

    }

    private void animateClockFontChange(Typeface newTypeface) {
        clock.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction(() -> {
                    clock.setTypeface(newTypeface);
                    clock.animate().alpha(1f).setDuration(150).start();
                })
                .start();
    }





}