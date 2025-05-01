package com.nemcut.launcher;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
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

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

public class SettingsActivity extends AppCompatActivity {

    private MaterialSwitch gridSwitch;
    private Slider gridWidth;
    private TextView widthLabel;
    private Button fontButton1;
    private Button fontButton2;
    private Button fontButton3;
    private TextClock clock;

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



        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean gridEnabled = prefs.getBoolean("grid_enabled", false);
        int gridColumns = prefs.getInt("grid_columns", 4);
        int clockFont = prefs.getInt("clockFont", 1);



        Typeface typeface = null;
        switch(clockFont) {
            case 1:
                typeface = ResourcesCompat.getFont(this,R.font.abeezee);
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
            var tf = ResourcesCompat.getFont(this,R.font.abeezee);
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

        getSupportActionBar().setTitle("Nastavení"); // nebo jiný text

        MaterialSwitch grid = findViewById(R.id.gridSwitch);

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);

        gridSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("grid_enabled", isChecked).apply();
        });

        gridWidth.addOnChangeListener((slider, value, fromUser) -> {
            widthLabel.setText(String.valueOf((int) value));
            prefs.edit().putInt("grid_columns", (int) value).apply();
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
}