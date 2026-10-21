package com.example.smartpantrymanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(
                "SmartPantrySettings",
                MODE_PRIVATE
        );

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(24), dp(24), dp(24));
        layout.setBackgroundColor(Color.rgb(246, 248, 243));

        TextView title = new TextView(this);
        title.setText("Settings");
        title.setTextSize(28);
        title.setTextColor(Color.rgb(36, 92, 67));
        title.setGravity(Gravity.CENTER_VERTICAL);

        layout.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(60)
        ));

        TextView description = new TextView(this);
        description.setText(
                "Manage your Smart Pantry preferences."
        );
        description.setTextSize(16);
        description.setTextColor(Color.DKGRAY);

        LinearLayout.LayoutParams descriptionParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        descriptionParams.bottomMargin = dp(24);
        layout.addView(description, descriptionParams);

        Switch expirySwitch = new Switch(this);
        expirySwitch.setText("Expiring-soon alerts");
        expirySwitch.setTextSize(16);
        expirySwitch.setTextColor(Color.BLACK);
        expirySwitch.setPadding(0, dp(12), 0, dp(12));

        expirySwitch.setChecked(
                preferences.getBoolean("expiry_alerts", true)
        );

        expirySwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) ->
                        preferences.edit()
                                .putBoolean("expiry_alerts", isChecked)
                                .apply()
        );

        layout.addView(expirySwitch);

        TextView note = new TextView(this);
        note.setText(
                "Your preference is saved on this device."
        );
        note.setTextSize(14);
        note.setTextColor(Color.DKGRAY);

        LinearLayout.LayoutParams noteParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        noteParams.topMargin = dp(16);
        layout.addView(note, noteParams);

        setContentView(layout);
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }
}
