package com.guesscity.guessthecity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

/**
 * Created by Igor on 24.07.15.
 */
public class SettingsActivity extends Activity implements View.OnClickListener {
    //    public static final String APP_PREFERENCES = "mysettings";
//    public static final String APP_PREFERENCES_CITY_GAME = "city_game";
    private SharedPreferences mSettings;
    private SharedPreferences.Editor editor;
    RadioButton radioCity, radioCountry;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        mSettings = getSharedPreferences(getResources().getString(R.string.APP_PREFERENCES).toString(), Context.MODE_PRIVATE);
        editor = mSettings.edit();

        radioCity = (RadioButton) findViewById(R.id.radioBtnCity);
        radioCountry = (RadioButton) findViewById(R.id.radioBtnCountry);
        radioCity.setOnClickListener(this);
        radioCountry.setOnClickListener(this);

        Boolean city = true;
        if (mSettings.contains(getResources().getString(R.string.APP_PREFERENCES_CITY_GAME).toString())) {
            // Получаем число из настроек
            city = mSettings.getBoolean(getResources().getString(R.string.APP_PREFERENCES_CITY_GAME).toString(), true);

        }
        if (city) {
            radioCity.setChecked(true);
        } else {
            radioCountry.setChecked(true);

        }


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.radioBtnCity:

                // Запоминаем данные
                editor.putBoolean(getResources().getString(R.string.APP_PREFERENCES_CITY_GAME).toString(), true);
                editor.apply();

                break;
            case R.id.radioBtnCountry:
                // Запоминаем данные
                editor.putBoolean(getResources().getString(R.string.APP_PREFERENCES_CITY_GAME).toString(), false);
                editor.apply();
                break;
        }
    }
}
