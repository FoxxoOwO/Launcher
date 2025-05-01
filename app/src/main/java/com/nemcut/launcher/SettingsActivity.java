package com.nemcut.launcher;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.materialswitch.MaterialSwitch;
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



        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        gridEnabled = prefs.getBoolean("grid_enabled", false);
        gridColumns = prefs.getInt("grid_columns", 4);
        clockFont = prefs.getInt("clockFont", 3);
        iconSize = prefs.getInt("iconSize", 54);
        textSize = prefs.getInt("textSize", 20);



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
            clock.setTypeface(tf);
            prefs.edit().putInt("clockFont", 1).apply();
        });

        fontButton2.setOnClickListener(v->{
            var tf = ResourcesCompat.getFont(this,R.font.akronim);
            clock.setTypeface(tf);
            prefs.edit().putInt("clockFont", 2).apply();
        });

        fontButton3.setOnClickListener(v->{
            var tf = ResourcesCompat.getFont(this,R.font.ar_one_sans);
            clock.setTypeface(tf);
            prefs.edit().putInt("clockFont", 3).apply();
        });

        gridSwitch.setChecked(gridEnabled);
        gridWidth.setValue(gridColumns);
        widthLabel.setText(String.valueOf(gridColumns));
        iconSizeSlider.setValue(iconSize);
        textSizeSlider.setValue(textSize);

        getSupportActionBar().setTitle("Nastavení"); // nebo jiný text

        MaterialSwitch grid = findViewById(R.id.gridSwitch);

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);


        setupLayout();

        gridSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("grid_enabled", isChecked).apply();
            gridEnabled = isChecked;
            setupLayout();
        });

        gridWidth.addOnChangeListener((slider, value, fromUser) -> {
            widthLabel.setText(String.valueOf((int) value));
            prefs.edit().putInt("grid_columns", (int) value).apply();
            gridColumns = (int) value;
            setupLayout();
        });

        iconSizeSlider.addOnChangeListener((slider, value, fromUser) -> {
            prefs.edit().putInt("iconSize", (int) value).apply();
            iconSize = (int) value;
            setupLayout();
        });

        textSizeSlider.addOnChangeListener((slider, value, fromUser) -> {
            prefs.edit().putInt("textSize", (int) value).apply();
            textSize = (int) value;
            setupLayout();
        });




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
        Adapter previewAdapter = new Adapter(this, previewList, gridEnabled,true, iconSize, textSize,gridColumns);
        previewRecycler.setAdapter(previewAdapter);
    }

}