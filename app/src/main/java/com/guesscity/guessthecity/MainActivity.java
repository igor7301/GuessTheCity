package com.guesscity.guessthecity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.startapp.android.publish.Ad;
import com.startapp.android.publish.AdEventListener;
import com.startapp.android.publish.StartAppAd;

import java.util.*;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    public static final String SOCIAL_NETWORK_TAG = "SocialIntegrationMain.SOCIAL_NETWORK_TAG";
    private ImageView imageViewHint;
    private ImageView imageViewMainPicture;
    private SharedPreferences mSettings;
    private SharedPreferences.Editor editor;
    private TextView textViewProgress;
    private LinearLayout linearLayoutEndGame;
    private LinearLayout linearLayoutAnswerButtons;
    private TextView textViewEndGameMessage;
    private TextView textViewEndGameMessageProgress;
    private RatingBar livesWidget;
    private Button btnRestartGame, btnQuitGame;
    private Button button1, button2, button3, button4;
    private HashMap pictures;
    private HashMap pictures_name;
    private HashMap pictures_hints;
    private Integer keyOfActivePicture;
    private List<Integer> remainderPicturesKey;
    private final Handler handler = new Handler();
    private Integer currentLives;
    private final static Integer LIVES = 3;
    private Bundle savedInstanceState;
    private final static String TEMPLATE = "_level";
    private final static List<Integer> AVAILABLE_LEVELS = Arrays.asList(1, 2, 3, 4, 5);
    private TextView textViewLevelInfo;
    private StartAppAd startAppAd = new StartAppAd(this);


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        setContentView(R.layout.main_activity);

        livesWidget = (RatingBar) findViewById(R.id.ratingBar);
        linearLayoutAnswerButtons = (LinearLayout) findViewById(R.id.linearLayoutAnswerButtons);
        linearLayoutEndGame = (LinearLayout) findViewById(R.id.linearLayoutEndGame);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        btnRestartGame = (Button) findViewById(R.id.buttonRestartGame);
        btnQuitGame = (Button) findViewById(R.id.buttonQuitGame);
        textViewEndGameMessage = (TextView) findViewById(R.id.textViewEndGameMessage);
        textViewEndGameMessageProgress = (TextView) findViewById(R.id.textViewEndGameProgressMessage);
        textViewProgress = (TextView) findViewById(R.id.textViewProgress);
        imageViewHint = (ImageView) findViewById(R.id.imageViewHint);
        imageViewMainPicture = (ImageView) findViewById(R.id.imageViewMainPicture);
        textViewLevelInfo = (TextView) findViewById(R.id.textViewLevelInfo);

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        btnRestartGame.setOnClickListener(this);
        btnQuitGame.setOnClickListener(this);
        imageViewHint.setOnClickListener(this);

        mSettings = getSharedPreferences(getResources().getString(R.string.APP_PREFERENCES).toString(), Context.MODE_PRIVATE);
        editor = mSettings.edit();
        Boolean city = mSettings.getBoolean(getResources().getString(R.string.APP_PREFERENCES_CITY_GAME).toString(), true);
        Integer level = mSettings.getInt(getResources().getString(R.string.APP_PREFERENCES_CURRENT_LEVEL), 1);
        setCurrentLevel(level);


        if (savedInstanceState == null) {

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SocialNetworkFragment())
                    .commit();
        }

        if (level == 0) {
            throw new RuntimeException("You level couldn't be equals 0");
        }

        pictures = (HashMap<String, String>) ResourceUtils.getHashMapResource(this, R.xml.pictures, TEMPLATE + level);

        if (city) {
            pictures_name = (HashMap<String, String>) ResourceUtils.getHashMapResource(this, R.xml.city_names);
            pictures_hints = (HashMap<String, String>) ResourceUtils.getHashMapResource(this, R.xml.city_hints);
        } else {
            pictures_name = (HashMap<String, String>) ResourceUtils.getHashMapResource(this, R.xml.country_names);
            pictures_hints = (HashMap<String, String>) ResourceUtils.getHashMapResource(this, R.xml.country_hints);
        }


        updateLives(LIVES);
        remainderPicturesKey = loadKey(pictures);
        keyOfActivePicture = getRandomValue(remainderPicturesKey);
        remainderPicturesKey.remove(remainderPicturesKey.indexOf(keyOfActivePicture));

        loadMainPicture(keyOfActivePicture);
        buttonsInitialization();

        updateProgress();




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(SOCIAL_NETWORK_TAG);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private Integer getAmountOfAllQuestionsOnLevel() {
        return pictures.size();
    }

    private Integer getAmountOfRemainderQuestionsOnLevel() {
        return remainderPicturesKey.size();
    }

    private void updateLives(Integer lives) {
        currentLives = lives;
        livesWidget.setRating(lives);
        livesWidget.setIsIndicator(true);
    }

    private void updateProgress() {
        textViewProgress.setText(getNumberOfActiveQuestionOnLevel() + "/" + getAmountOfAllQuestionsOnLevel());
        textViewLevelInfo.setText(getString(R.string.current_level) + getCurrentLevel() + "/" + Collections.max(AVAILABLE_LEVELS));
    }

    private Integer getAmountOfAllQuestionInGame() {
        return  ((HashMap < String, String >) ResourceUtils.getHashMapResource(this, R.xml.pictures, TEMPLATE)).size();
    }

    private Integer getNumberOfActiveQuestionInGame() {

        int numberOfActiveQuestionInGame = 0;

        for(int i = Collections.min(AVAILABLE_LEVELS); i <= (getCurrentLevel() - 1); i++ ) {
            numberOfActiveQuestionInGame +=
                    ((HashMap < String, String >) ResourceUtils.getHashMapResource(this, R.xml.pictures, TEMPLATE + i))
                            .size();


        }
        numberOfActiveQuestionInGame += getNumberOfActiveQuestionOnLevel();
        return numberOfActiveQuestionInGame;


    }


    private void processEndGame() {

        String message;
        linearLayoutAnswerButtons.setVisibility(View.INVISIBLE);
//        relativeLayoutTopBar.setAlpha((float) 0.5);

//        textViewEndGameMessage.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/LeagueGothicRegular.otf"));

        if (currentLives > 0) {
            message = getString(R.string.you_won_message);

        } else {
            message = getString(R.string.you_loose_message);
        }
        textViewEndGameMessage.setGravity(Gravity.CENTER);
        textViewEndGameMessage.setText(message);
        textViewEndGameMessageProgress.setText(getString(R.string.end_game_progress_message) +
                " " + getNumberOfActiveQuestionInGame() + "/" + getAmountOfAllQuestionInGame());
        linearLayoutEndGame.setVisibility(View.VISIBLE);
        editor.putInt(getResources().getString(R.string.APP_PREFERENCES_CURRENT_LEVEL), 1);
        editor.apply();

    }

    private Integer getNumberOfActiveQuestionOnLevel() {
        return (getAmountOfAllQuestionsOnLevel() - getAmountOfRemainderQuestionsOnLevel());
    }


    private void goToNextQuestion() {
        keyOfActivePicture = getRandomValue(remainderPicturesKey);
        remainderPicturesKey.remove(remainderPicturesKey.indexOf(keyOfActivePicture));
        loadMainPicture(keyOfActivePicture);
        buttonsInitialization();
        updateProgress();

    }

    private Boolean lastQuestion() {
        return remainderPicturesKey.size() > 0 ? false : true;
    }


    private void setAllButtonsClickable(Boolean condition) {

        button1.setClickable(condition);
        button2.setClickable(condition);
        button3.setClickable(condition);
        button4.setClickable(condition);


    }

    private Integer getCurrentLevel() {
        return mSettings.getInt(getResources().getString(R.string.APP_PREFERENCES_CURRENT_LEVEL), 0);
    }

    private void setCurrentLevel(Integer level) {
        editor.putInt(getResources().getString(R.string.APP_PREFERENCES_CURRENT_LEVEL), level);
        editor.apply();
    }


    private void processRightAnswer(Button button) {

        setAllButtonsClickable(false);
        processRightButton(button);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {


                if (!lastQuestion() && currentLives != 0) {
                    goToNextQuestion();

                } else {

                    goToNextLevel();
                }
            }
        }, 1000);


    }

    @Override
    public void onResume() {
        super.onResume();
        startAppAd.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        startAppAd.onPause();
    }


    private void goToNextLevel() {
        if (lastLevel() || currentLives == 0) {
            processEndGame();
        } else {
            finish();
            setCurrentLevel(getCurrentLevel() + 1);
            Intent intent = new Intent(this, LevelUpActivity.class);
            startActivity(intent);
            startAppAd.showAd();
            startAppAd.loadAd();
        }
    }

    private boolean lastLevel() {

        int level = mSettings.getInt(getResources().getString(R.string.APP_PREFERENCES_CURRENT_LEVEL), 0);
        if (level == Collections.max(AVAILABLE_LEVELS)) {
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        startAppAd.onBackPressed();
        super.onBackPressed();
        finish();
        setCurrentLevel(1);
    }

    private void processWrongAnswer(Button button) {

        setAllButtonsClickable(false);
        currentLives--;
        updateLives(currentLives);
        processWrongButton(button);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                processRightAnswer(getCorrectAnswerButton());
            }
        }, 1000);


    }

    private void processWrongButton(Button button) {
        button.setClickable(false);
        button.setBackgroundResource(R.anim.incorrect_button);
        AnimationDrawable anim = (AnimationDrawable) button.getBackground();
        anim.start();

    }


    private void processRightButton(Button button) {
        button.setClickable(false);
        button.setBackgroundResource(R.anim.correct_button);
        AnimationDrawable anim = (AnimationDrawable) button.getBackground();
        anim.start();
    }

    public ImageView getImageViewMainPicture() {
        return imageViewMainPicture;
    }

    private Button getCorrectAnswerButton() {
        if ((Integer) button1.getTag() == (Integer) getImageViewMainPicture().getTag()) {

            return button1;
        } else if ((Integer) button2.getTag() == (Integer) getImageViewMainPicture().getTag())

        {
            return button2;
        } else if ((Integer) button3.getTag() == (Integer) getImageViewMainPicture().getTag())

        {
            return button3;
        } else if ((Integer) button4.getTag() == (Integer) getImageViewMainPicture().getTag())

        {
            return button4;
        }
        return null;


    }

    private void processTheAnswer(Button button) {


        if ((Integer) button.getTag() == (Integer) getImageViewMainPicture().getTag()) {
            processRightAnswer(button);


        } else {
            processWrongAnswer(button);
        }


    }

    @Override
    public void onClick(View view) {


        switch (view.getId()) {
            case R.id.button1:
                processTheAnswer(button1);
                break;
            case R.id.button2:
                processTheAnswer(button2);
                break;
            case R.id.button3:
                processTheAnswer(button3);
                break;
            case R.id.button4:
                processTheAnswer(button4);
                break;
            case R.id.buttonRestartGame:
                onCreate(savedInstanceState);
                break;
            case R.id.buttonQuitGame:
                onBackPressed();
                break;
            case R.id.imageViewHint:
                processHint();
                break;

            default:
                break;

        }
    }


    private void processHint() {

        Toast toast = Toast.makeText(this, getHint(keyOfActivePicture), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        LinearLayout layout = (LinearLayout) toast.getView();

        if (layout.getChildCount() > 0)
        {
            TextView SampleView = (TextView) layout.getChildAt(0);
            SampleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        }

        toast.show();

    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        return BitmapFactory.decodeResource(res, resId, options);
    }

    private String getHint(Integer key) {
        return (String) pictures_hints.get(key.toString());
    }


    private List<Integer> loadKey(Map<String, String> map) {
        List<Integer> keyList = new ArrayList<Integer>();
        for (Object key : map.keySet()) {
            keyList.add(Integer.parseInt((String) key));
        }
        return keyList;
    }

    private <T> T getRandomValue(List<T> list) {

        Integer randInt = new Random().nextInt(list.size());

        T retValue = list.get(randInt);

        return retValue;
    }


    private void loadMainPicture(Integer num) {
        int res = getResources()

                .getIdentifier((String) pictures.get((num).toString()), "drawable", "com.guesscity.guessthecity");

        imageViewMainPicture.setImageBitmap(decodeSampledBitmapFromResource(getResources(), res,
                480, 480));

        imageViewMainPicture.setTag(num);

    }


    private void buttonsInitialization() {

        List<Integer> picturesKey = loadKey(pictures);
        Integer key1, key2, key3, key4;
        String value1 = null, value2 = null, value3 = null, value4 = null;


        key1 = keyOfActivePicture;
        value1 = ((String) pictures_name.get(key1.toString())).trim();
        do {

            if (picturesKey.size() == 0) {
                key2 = key1;
                break;
            } else {
                key2 = getRandomValue(picturesKey);
                picturesKey.remove(picturesKey.indexOf(key2));
                value2 = ((String) pictures_name.get(key2.toString())).trim();
            }

        } while (value1.equalsIgnoreCase(value2));

        do {

            if (picturesKey.size() == 0) {
                key3 = key2;
                break;
            } else {
                key3 = getRandomValue(picturesKey);
                picturesKey.remove(picturesKey.indexOf(key3));
                value3 = ((String) pictures_name.get(key3.toString())).trim();
            }

        } while (value3.equalsIgnoreCase(value1) || value3.equalsIgnoreCase(value2));


        do {

            if (picturesKey.size() == 0) {
                key4 = key3;
                break;
            } else {
                key4 = getRandomValue(picturesKey);
                picturesKey.remove(picturesKey.indexOf(key4));
                value4 = ((String) pictures_name.get(key4.toString())).trim();
            }

        } while (value4.equalsIgnoreCase(value1) || value4.equalsIgnoreCase(value2) || value4.equalsIgnoreCase(value3));


        List<Integer> shuffleKeys = Arrays.asList(key1, key2, key3, key4);
        Collections.shuffle(shuffleKeys);

        button1.setText(pictures_name.get(shuffleKeys.get(0).toString()).toString());
        button2.setText(pictures_name.get(shuffleKeys.get(1).toString()).toString());
        button3.setText(pictures_name.get(shuffleKeys.get(2).toString()).toString());
        button4.setText(pictures_name.get(shuffleKeys.get(3).toString()).toString());

        button1.setTag(shuffleKeys.get(0));
        button2.setTag(shuffleKeys.get(1));
        button3.setTag(shuffleKeys.get(2));
        button4.setTag(shuffleKeys.get(3));

        initDefaultButton(button1);
        initDefaultButton(button2);
        initDefaultButton(button3);
        initDefaultButton(button4);

        linearLayoutEndGame.setVisibility(View.INVISIBLE);


    }


    private void initDefaultButton(Button button) {
        button.setClickable(true);
        button.setBackgroundResource(R.drawable.default_button);
    }

}
