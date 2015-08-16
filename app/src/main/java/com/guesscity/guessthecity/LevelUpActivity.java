package com.guesscity.guessthecity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.startapp.android.publish.StartAppAd;

/**
 * Created by Igor on 01.08.15.
 */
public class LevelUpActivity extends Activity implements View.OnClickListener {
    private StartAppAd startAppAd = new StartAppAd(this);
    private TextView textViewInfo;
    private ImageView imageViewLevelUp;
    private Button buttonContinue;
    private SharedPreferences mSettings;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.level_up_activity);

        textViewInfo = (TextView) findViewById(R.id.textViewLevelUpInfo);
        buttonContinue = (Button) findViewById(R.id.buttonContinue);


        mSettings = getSharedPreferences(getResources()
                .getString(R.string.APP_PREFERENCES).toString(), Context.MODE_PRIVATE);
        editor = mSettings.edit();

        textViewInfo.setOnClickListener(this);

        textViewInfo.setText(getString(R.string.new_level_message) +  " " + mSettings.getInt(getResources().getString(R.string.APP_PREFERENCES_CURRENT_LEVEL), 0));
//        int res = getResources()
//
//                .getIdentifier("level_up" + mSettings.getInt(getResources()
//                .getString(R.string.APP_PREFERENCES_CURRENT_LEVEL), 0), "drawable", "com.guesscity.guessthecity");
//
//        imageViewLevelUp.setImageResource(res);

        buttonContinue.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        startAppAd.onResume();
    }

    @Override
    public void onClick(View view) {
      switch (view.getId()) {
          case R.id.buttonContinue:
              finish();
              Intent intent = new Intent(this, MainActivity.class);
              startActivity(intent);
              break;
      }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        editor.putInt(getResources().getString(R.string.APP_PREFERENCES_CURRENT_LEVEL), 1);
        editor.apply();
    }
}
