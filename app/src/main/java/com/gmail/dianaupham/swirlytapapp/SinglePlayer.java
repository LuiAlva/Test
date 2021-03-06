package com.gmail.dianaupham.swirlytapapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.gmail.dianaupham.swirlytapapp.swirlytap.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class SinglePlayer extends Activity implements View.OnClickListener {
    int Score = 0, Combo = 0, ComboMax = 0;          //this is total score
    int GoodSwirlTotalPass = 0; //For passing old GoodSwirl total
    int TwiceSwirlTotalPass = 0;//For passing old TwiceSwirl total
    int BadSwirlTotalPass = 0;  //For passing old BadSwirl total
    int TimeAddTotalPass = 0;   //For passing old TimeAdd total
    int SP_GamesPlayedTotalPass = 0;//passing old SP_GamesPlayed total
    String NAME = "";       // For Player Name for CompareScores()
    public static final String PREFS_NAME = "PREFS_FILE"; // Name of preference file
    boolean addTime = false;    //Allows Time button to appear
    boolean paused = false;
    boolean Countdown_active = false; //DisablePause
    boolean NukeActivated = false;
    boolean LightningBombActivated = false;
    boolean DoubleBombActivated = false;
    boolean DOUBLEPOINTS = false;       // For double points
    PopupWindow popupWindow; // Popup Window for Countdown, Pause menu, and Time over
    TableLayout GameWindow;  // For SwirlTable background
    ImageButton PauseButton; // create image button for pause
    ImageButton Nuke, LightningBomb, DoublingBomb;        // Create Image button for Nuke
    TextView ComboText;      // For Combo Text
    ProgressBar Speed_Bar;   // Speed bar
    //Grid & Storage ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    private static final int NUM_ROWS = 6; //instantiated size of grid
    private static final int NUM_COLS = 4;
    private static final int ARRAY_ROWS = NUM_ROWS * 5;
    private static final int ARRAY_COLS = NUM_COLS * 5;
    int randRow;
    int randCol;
    private static int FINAL_COL; //set col and row location to pass to gridButton
    private static int FINAL_ROW; //this sends location of button
    Button buttons[][] = new Button[NUM_ROWS][NUM_COLS];   //created total number of grid buttons
    String[][] luckArray = new String[ARRAY_ROWS][ARRAY_COLS]; //array containing good and bad buttons
    //Time & Speed ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    int StartTime = 91000;       //Set start time, 60000
    int Current_Time = 91000;    //Current in-game time
    int Current_Speed = 290;     //Current in-game speed
    int Start_Speed = 290;       //Speed at the start of the game
    int Speed_Limit = 90;        //Highest Speed
    int Game_Speed_Add = 10;     //Add speed every increment
    int Speed_Increment = 10;     //Points needed to increment speed
    int Speed_Increment_Add = 0; //Add to Speed_Increment to make it harder to speed up
    int Speed_Increment_Set = 10; //Equal to Speed_Increment
    //Music & Sounds ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    Uri tapGood;              // Sound when Good swirl is tapped
    Uri tapBad;               // Sound for Bad swirl
    Uri tapTimeAdd;           // Sound for Time add swirl
    Uri CountdownSound;       // Sound for Countdown at the beginning of the game
    Uri CountdownGoSound;     // Sound for Start after Countdown
    Uri NukeBoom, ThunderBoom; // For Nuke Explosion
    MediaPlayer GoodSound;    // MediaPlayer for playing good sound
    MediaPlayer GoodSound2;   // MediaPlayer for playing good sound - for faster sound
    MediaPlayer BadSound;     // MediaPlayer for playing Bad sound
    MediaPlayer SpecialSound; // MediaPlayer for playing Special button sounds
    MediaPlayer gameBG;       // For Background music
    //Timers ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    CountDownTimer Updater;        //Timer that updates test and values at 30 frames/ms(millisecond)
    CountDownTimer TimeCountdown;  //Timer that updates the timer every second
    CountDownTimer SwirlEngine;    //Timer that lets the swirls appear. Update depends on Start_Speed
    CountDownTimer CountdownTimer; //Timer for the countdown at the beginning
    //Swirl Disappear System ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    buttonDisappear[] GoodArray = new buttonDisappear[15];
    buttonDisappear[] BadArray = new buttonDisappear[15];
    buttonDisappear[] SpecialArray = new buttonDisappear[15];
    int Good_Pressed = 0;       // GoodSwirls pressed
    int Bad_Pressed = 0;        // BadSwirls pressed
    int Time_Pressed = 0;       // TimeSwirls pressed
    int Good2_Pressed = 0;      // 2xGoodSwirls pressed
    int OnScreenGood = 0;       // GoodSwirls On Screen
    int OnScreenBad = 0;        // BadSwirls On Screen
    int OnScreenTime = 0;       // TimeSwirls On Screen
    int OnScreenGood2 = 0;      // 2xGoodSwirls On Screen
    int spFirstGamePlayed = 1;  // Set #game played to 1 after game finish, if first game played
    int Extra_Time_Max = 1;     // Set limit for amount of time added (20 seconds)
    int Extra_Time_Counter = 0; // Count amount of ExtraTime added
    String screenshot;          // Screenshot (file) of SinglePlayer
    Bitmap bitmap;              // Bitmap of screenshot of SinglePlayer
    Vibrator vibration;         // Device will vibrate when BadSwirl is clicked

    private static final boolean AUTO_HIDE = true;          // Auto hide UI (ActionBar)
    private static final int AUTO_HIDE_DELAY_MILLIS = 1000; // Hide system UI after 1000 milliseconds
    private static final boolean TOGGLE_ON_CLICK = true;    // If UI is clicked show it
    private static final int HIDER_FLAGS = 0;   // The flags to pass to {@link com.gmail.dianaupham.swirlytap.SystemUiHider#getInstance}.
    private SystemUiHider mSystemUiHider;
    // TODO: Replace this test id with your personal ad unit id
    //private static final String MOPUB_BANNER_AD_UNIT_ID = "d4a0aba637d64a9f9a05a575fa757ac2";
    //private MoPubView moPubView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE); // Removes Title Bar
        //View decorView = getWindow().getDecorView();   // Hide action Bar
        //int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        //decorView.setSystemUiVisibility(uiOptions);
        //Hide Actionbar ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);   //Hides the action and title bars!
        getActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_single_player); //show res/layout/activity_single_player.xml
        vibration = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE); //set up device vibration control
        ComboText = (TextView)findViewById(R.id.ComboText);
        //Buttons +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
        PauseButton = (ImageButton)findViewById(R.id.pause_button);
        PauseButton.setOnClickListener(this); //sets an onClickListener on PauseButton
        Nuke = (ImageButton)findViewById(R.id.Nuke);
        Nuke.setOnClickListener(this);         //sets an onClickListener on PauseButton
        Nuke.setEnabled(false);               //Start Nuke Disabled
        Nuke.setClickable(false);
        LightningBomb = (ImageButton)findViewById(R.id.LightningButton); // For lightning Bomb
        LightningBomb.setOnClickListener(this);
        LightningBomb.setEnabled(false);
        LightningBomb.setClickable(false);
        DoublingBomb = (ImageButton)findViewById(R.id.DoubleButton); // For Double points Bomb
        DoublingBomb.setOnClickListener(this);
        DoublingBomb.setEnabled(false);
        DoublingBomb.setClickable(false);
        Speed_Bar = (ProgressBar)findViewById(R.id.SpeedBar);
        GameWindow = (TableLayout)findViewById(R.id.tableForButtons);
        //Sounds ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
        gameBG = MediaPlayer.create(this, R.raw.game_song); //get background song
        gameBG.setLooping(true);    //make background song loop
        GoodSound = new MediaPlayer();     // Setup MediaPlayer for good sound
        GoodSound2 = new MediaPlayer();    // Setup MediaPlayer for good sound
        BadSound = new MediaPlayer();      // Setup MediaPlayer for bad sound
        SpecialSound = new MediaPlayer();  // Setup MediaPlayer for Special button sounds
        GoodSound.setVolume(12,12);
        GoodSound2.setVolume(12,12);
        CountdownSound = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.countdown);    //Setup sound for Countdown
        CountdownGoSound = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.countdown_go);//Setup sound for Start sound
        tapGood = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.tap_good);    //Setup sound for Good swirl
        tapBad = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.tap_bad);      //Setup sound for Bad swirl
        tapTimeAdd = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.tap_time); //Setup sound for Time up swirl
        NukeBoom = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.nuke_explosion);    //Setup sound for Nuke Boom
        ThunderBoom = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.bomb_freeze);    //Setup sound for Nuke Boom

        //Nuke.setImageResource(R.drawable.nuke_button_active); // Activate Nuke for now
        //Nuke.setEnabled(true);
        //LightningBomb.setImageResource(R.drawable.lightning_button_active); // Activate Nuke for now
        //LightningBomb.setEnabled(true);

        //moPubView = (MoPubView) findViewById(R.id.mopub_sample_ad);
        //moPubView.setAdUnitId(MOPUB_BANNER_AD_UNIT_ID);
        //moPubView.loadAd();

        populateButtons(); //add buttons to grid

        ///////////////////////////////////////POPULATE LUCK ARRAY///////////////////////////////////

        //POPULATE luck array with different types of buttons
        Random rand = new Random(); //randomly select location in luck array
        rand.setSeed(System.currentTimeMillis());
        for(int row = 0; row<ARRAY_ROWS; row++)
        {
            for(int col = 0; col<ARRAY_COLS; col++)
            {
                //bad button should display in a more random fashion...please test
                int randCell = rand.nextInt(ARRAY_COLS*ARRAY_ROWS); //random selection of numbers
                if(randCell%5 ==0) //much less chance to receive bad button
                {
                    luckArray[row][col] = "bad"; //place bad button in array
                }
                else if(randCell%6 == 0)
                {
                    luckArray[row][col] = "twicePoints"; //place 2x button in array
                }
                else if(randCell%21 == 0)  //TODO: make addTime more random
                {
                    luckArray[row][col] = "addTime"; //place add time button in array
                }
                else
                    luckArray[row][col] = "good"; //much higher chance to receive good button
            }
        }
        /////////////////////////////////////////////////////////////////////////////////////////
        // Null Disappear timers array
        for(int i = 0; i < 15; i++){
            GoodArray[i] = null;
            BadArray[i] = null;
            SpecialArray[i] = null;
        }

        //Wait for game to load
        new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (millisUntilFinished / 1000 == 0) {
                    onFinish();
                } else {
                }
            }
            @Override
            public void onFinish() {
                GameStart();       //Start beginning Countdown
            }
        }.start();
    }

    private void populateButtons() //creating grid of buttons. These buttons are initialized as disabled and invisible
    {
        TableLayout table = (TableLayout)findViewById(R.id.tableForButtons); //make new table
        for(int row = 0; row < NUM_ROWS; row++) //rows instantiated at top
        {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.MATCH_PARENT,1.0f)); //make layout look nice
            table.addView(tableRow);
            for(int col = 0; col < NUM_COLS; col++) //cols instantiated at top
            {
                FINAL_COL = col; //set col and row location to pass to gridButton
                FINAL_ROW = row; //this sends location of button
                Button Swirl = new Button(this); //create button to display correctly
                Swirl.setBackgroundResource(R.drawable.goodswirl); //make this grid block location have the image of goodswirl
                Swirl.setVisibility(View.INVISIBLE);               //Start Swirl Invisible
                Swirl.setEnabled(false);                           //Start Swirl button Disabled
                Swirl.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT,1.0f));

                //This sets what the swirl will do when clicked
                Swirl.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setVisibility(View.INVISIBLE);         // Make Swirl disappear
                        v.setEnabled(false);                     // Disable button
                        Score++;                                 // Add one to score
                    }
                });
                tableRow.addView(Swirl);
                buttons[row][col] = Swirl;
            }
        }//end 'for'
    }//end private void populateButtons

    void GameTimers(int Time) { // contains the timers of the game

        TimeCountdown = new CountDownTimer(Time, 1000)
        {
            //create new type textview and relate it to countdown textbox in activity_single_player.xml
            TextView mTextField = (TextView) findViewById(R.id.countdown);

            public void onTick(long millisUntilFinished)
            {
                if (millisUntilFinished / 1000 == 0)
                {
                    onFinish();
                }
                else
                Current_Time = (int) millisUntilFinished;         //Updates current time
                mTextField.setText("" + millisUntilFinished / 1000);  //display seconds left in text field
            }
            //stop time/game when time is up
            public void onFinish() {
                mTextField.setText("0");                              //Set end of timer
                SwirlEngine.cancel();
                Updater.cancel();
                DestroySwirls();
                GameOver();
            }
        }.start();

        Updater = new CountDownTimer(Time, 30)
        {
            //Get access to totalScore Textbox
            TextView totalScore= (TextView) findViewById(R.id.totalScore);

            public void onTick(long millisUntilFinished)
            {
                //Update totalScore Textbox with current score, end at 60 seconds
                if (millisUntilFinished / 30 == 0)
                {
                    onFinish();
                }
                else
                {
                    totalScore.setText("" + Score); //Update Score Counter
                    ComboText.setText("Combo: " + Combo); //Update Score Counter
                    if (Current_Speed != Speed_Limit)
                       Speed_Engine(Good_Pressed + Good2_Pressed);           //Update the Speed
                }
            }
            // Show text at end of timer
            public void onFinish() {
                totalScore.setText("" + Score); //Update Score Counter
            }
        }.start();

        SwirlEngine = new CountDownTimer(Time, Current_Speed) {
            public void onTick(long millisUntilFinished)
            {
                if (millisUntilFinished / 30 == 0)
                {
                    onFinish();
                }
                else
                {
                    displayButton();
                }
            }
            public void onFinish()
            {

            }
        }.start();
    } // End of GameTimers

    class buttonDisappear{
        public Button ButtonId;
        CountDownTimer TimerId;
        int[] Position = { -1, -1};
        public buttonDisappear(Button button, CountDownTimer timer , int Row, int Col){
            ButtonId = button;
            TimerId = timer;
            Position[0] = Row;
            Position[1] = Col;
        }
    }

    public void displayButton() //will call when button needs to be displayed
    {
        final Runnable buttonRunnable;
        final Handler buttonHandler = new Handler();
        int i; //For array location for disappear
        do {
            Random r = new Random(); //randomly select location in luck array
            r.setSeed(System.currentTimeMillis());
            randRow = r.nextInt(NUM_ROWS);
            randCol = r.nextInt(NUM_COLS);
            for(i = 0; i < 15; i++){
                if (GoodArray[i] != null && GoodArray[i].Position[0] == randRow && GoodArray[i].Position[1] == randCol) { break; }
                if (BadArray[i] != null && BadArray[i].Position[0] == randRow && BadArray[i].Position[1] == randCol) { break; }
                if (SpecialArray[i] != null && SpecialArray[i].Position[0] == randRow && SpecialArray[i].Position[1] == randCol) { break; }
            }
        } while(i != 15);

        if(luckArray[randRow][randCol]=="good") //GOOD BUTTON +1 POINT IF CLICKED
        {
            for(i = 0; i < 10; i++) {        // Find empty spot in GoodArray
                if(GoodArray[i] == null){break;}
                if(i == 9) {i = 0;}
            }
            final Button goodButton = buttons[randRow][randCol];     //Button in this location
            goodButton.setBackgroundResource(R.drawable.goodswirl); //Set image to goodswirl
            OnScreenGood++;                                         //Count on Screen
            goodButton.setEnabled(true);                            //Enable Swirl
            goodButton.setVisibility(View.VISIBLE);                 //Make Swirl Visible
            final int finalI = i;
            CountDownTimer temp = new CountDownTimer(1000,1000) { // Set timer for disappearance
            public void onTick(long millisUntilFinished)
            {
                if (millisUntilFinished / 1000 == 0)
                {
                    onFinish();
                }
                else{}
            }

            @Override
            public void onFinish() {
                GoodArray[finalI].ButtonId = null;       // Remove Button ID
                GoodArray[finalI] = null;
                OnScreenGood--;
                goodButton.setVisibility(View.INVISIBLE);
                goodButton.setEnabled(false);
            }
            }.start();
            GoodArray[i] =  new buttonDisappear(goodButton, temp, randRow, randCol);
            goodButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {
                    {
                        Animation FadeAnim = new AlphaAnimation(1.0f, 0.0f);//fade out the text
                        FadeAnim.setDuration(200);
                        playGood(tapGood);
                        GoodArray[finalI].TimerId.cancel();      // Cancel it's disappear Timer
                        GoodArray[finalI].ButtonId = null;       // Remove Button ID
                        GoodArray[finalI] = null;                // Remove from array
                        OnScreenGood--;
                        goodButton.setBackgroundResource(R.drawable.goodswirl_break); //Set image to goodswirl
                        v.startAnimation(FadeAnim);
                        v.setVisibility(View.INVISIBLE);         // Make Swirl disappear when clicked
                        v.setEnabled(false);                     // Disable button
                        if(DOUBLEPOINTS) {
                            Score+= 2;                                 // Add two to score
                        } else { Score++; }                            // Add one to score
                        Good_Pressed++;                                // Pressed One
                        Combo++;                                       // Add 1 to combo
                    }
                }
            });
            //set 1 second timer... if timer reached then make button disappear
        }
        else if(luckArray[randRow][randCol] == "bad")
    {
        for(i = 0; i < 10; i++) {        // Find empty spot in BadArray
            if(BadArray[i] == null){break;}
            if(i == 9) {i = 0;}
        }
        OnScreenBad++;                                         // Count on screen
        final Button badButton = buttons[randRow][randCol];    //Button in this location
        badButton.setBackgroundResource(R.drawable.badswirl);  //Set image to BadSwirl
        badButton.setEnabled(true);                            //Enable Swirl
        badButton.setVisibility(View.VISIBLE);                 //Make Swirl Visible
        final int finalI = i;
        CountDownTimer temp = new CountDownTimer(1100,1100) { // Set timer for disappearance
            public void onTick(long millisUntilFinished)
            {
                if (millisUntilFinished / 1100 == 0)
                {
                    onFinish();
                }
                else{}
            }
            @Override
            public void onFinish() {
                BadArray[finalI] = null;
                OnScreenBad--;
                badButton.setVisibility(View.INVISIBLE);
                badButton.setEnabled(false);
            }
        }.start();
        BadArray[i] =  new buttonDisappear(badButton, temp, randRow, randCol);
        badButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
            {
                Animation FadeAnim = new AlphaAnimation(1.0f, 0.0f);//fade out the text
                FadeAnim.setDuration(200);
                playBad(tapBad);                        // Play tapBad
                vibration.vibrate(300);                 // Vibrate device for 300 milliseconds
                BadArray[finalI].TimerId.cancel();      // Cancel it's disappear Timer
                BadArray[finalI].ButtonId = null;       // Remove Button ID
                Bad_Pressed++;
                OnScreenBad--;
                v.setBackgroundResource(R.drawable.badswirl_break); //Set image to goodswirl
                v.startAnimation(FadeAnim);
                BadArray[finalI] = null;                // Remove from array
                v.setVisibility(View.INVISIBLE);        // Make Swirl disappear when clicked
                v.setEnabled(false);                    // Disable button
                if(Combo > ComboMax) { ComboMax = Combo; }
                Combo = 0;                              // Reset Combo
                Score -= 5;                             // Subtract 5 from score
            }
            }
        });
    }
    else if(luckArray[randRow][randCol] == "twicePoints")
    {
        for(i = 0; i < 10; i++) {        // Find empty spot in SpecialArray
            if(SpecialArray[i] == null){break;}
            if(i == 9) {i = 0;}
        }
        OnScreenGood2++;
        final Button twiceButton = buttons[randRow][randCol];     //Button in this location
        twiceButton.setBackgroundResource(R.drawable.twiceswirl); //Set image to goodswirl
        twiceButton.setEnabled(true);                            //Enable Swirl
        twiceButton.setVisibility(View.VISIBLE);                 //Make Swirl Visible
        final int finalI = i;
        CountDownTimer temp = new CountDownTimer(1000,1000) { // Set timer for disappearance
            public void onTick(long millisUntilFinished)
            {
                if (millisUntilFinished / 1000 == 0)
                {
                    onFinish();
                }
                else{}
            }

            @Override
            public void onFinish() {
                SpecialArray[finalI].ButtonId = null;       // Remove Button ID
                SpecialArray[finalI] = null;
                OnScreenGood2--;
                twiceButton.setVisibility(View.INVISIBLE);
                twiceButton.setEnabled(false);
            }
        }.start();
        SpecialArray[i] =  new buttonDisappear(twiceButton, temp, randRow, randCol);
        twiceButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
            {
                Animation FadeAnim = new AlphaAnimation(1.0f, 0.0f);//fade out the text
                FadeAnim.setDuration(200);
                playGood2(tapGood);
                SpecialArray[finalI].TimerId.cancel();      // Cancel it's disappear Timer
                SpecialArray[finalI].ButtonId = null;       // Remove Button ID
                SpecialArray[finalI] = null;                // Remove from array
                OnScreenGood2--;
                v.setBackgroundResource(R.drawable.twiceswirl_break); //Set image to goodswirl
                v.startAnimation(FadeAnim);
                v.setVisibility(View.INVISIBLE);         // Make Swirl disappear when clicked
                v.setEnabled(false);                     // Disable button
                if(DOUBLEPOINTS) {
                    Score+= 4;                                 // Add two to score
                } else { Score+= 2; }                            // Add one to score
                Good2_Pressed++;                                // Pressed One
                Combo++;                                       // Add 1 to combo
            }
            }
        });
    }
    else if(luckArray[randRow][randCol] == "addTime") {
            if (addTime == true /*&& Extra_Time_Counter <= Extra_Time_Max*/) {
                addTime = false;
                if (Extra_Time_Counter < Extra_Time_Max)
                {
                    for (i = 0; i < 10; i++) {        // Find empty spot in SpecialArray
                        if (SpecialArray[i] == null) {
                            break;
                        }
                        if (i == 9) {
                            i = 0;
                        }
                    }
                    OnScreenTime++;
                    final Button timeButton = buttons[randRow][randCol];     //Button in this location
                    timeButton.setBackgroundResource(R.drawable.fivetime); //Set image to goodswirl
                    timeButton.setEnabled(true);                            //Enable Swirl
                    timeButton.setVisibility(View.VISIBLE);                 //Make Swirl Visible
                    final int finalI = i;
                    CountDownTimer temp = new CountDownTimer(1000, 1000) { // Set timer for disappearance
                        public void onTick(long millisUntilFinished) {
                            if (millisUntilFinished / 1000 == 0) {
                                onFinish();
                            } else {
                            }
                        }

                        @Override
                        public void onFinish() {
                            SpecialArray[finalI].ButtonId = null;   // Remove Button ID
                            SpecialArray[finalI] = null;
                            OnScreenTime--;
                            timeButton.setVisibility(View.INVISIBLE);
                            timeButton.setEnabled(false);
                        }
                    }.start();
                    SpecialArray[i] = new buttonDisappear(timeButton, temp, randRow, randCol);
                    timeButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                        Animation FadeAnim = new AlphaAnimation(1.0f, 0.0f);//fade out the text
                        FadeAnim.setDuration(200);
                        playSpecial(tapTimeAdd);                      // Play time up sound
                        SpecialArray[finalI].TimerId.cancel();        // Cancel it's disappear Timer
                        SpecialArray[finalI].ButtonId = null;         // Remove Button ID
                        SpecialArray[finalI] = null;                  // Remove from array
                        v.startAnimation(FadeAnim);
                        v.setVisibility(View.INVISIBLE);              // Make Swirl disappear when clicked
                        v.setEnabled(false);                          // Disable button
                        SwirlEngine.cancel();                         // Cancel old Timers
                        Updater.cancel();
                        TimeCountdown.cancel();
                        Current_Time += 5000;                         // Add 5 seconds to time
                        GameTimers(Current_Time);                     // Update Timers
                        Time_Pressed++;
                        OnScreenTime--;                               // Remove 5 seconds from on-screen clock
                        Extra_Time_Counter++;                         // Add one to counter (max amount = 4)
                        }
                    });
                }//end 'if'
            } else if (addTime == false) {
                for(i = 0; i < 10; i++) {        // Find empty spot in GoodArray
                    if(GoodArray[i] == null){break;}
                    if(i == 9) {i = 0;}
                }
                OnScreenGood++;
                final Button goodButton = buttons[randRow][randCol];    //Button in this location
                goodButton.setBackgroundResource(R.drawable.goodswirl); //Set image to goodswirl
                goodButton.setEnabled(true);                            //Enable Swirl
                goodButton.setVisibility(View.VISIBLE);                 //Make Swirl Visible
                final int finalI = i;
                CountDownTimer temp = new CountDownTimer(1000,1000) { // Set timer for disappearance
                    public void onTick(long millisUntilFinished)
                    {
                        if (millisUntilFinished / 1000 == 0)
                        {
                            onFinish();
                        }
                        else{}
                    }
                    @Override
                    public void onFinish() {
                        GoodArray[finalI].ButtonId = null;       // Remove Button ID
                        GoodArray[finalI] = null;
                        OnScreenGood--;
                        goodButton.setVisibility(View.INVISIBLE);
                        goodButton.setEnabled(false);
                    }
                }.start();
                GoodArray[i] =  new buttonDisappear(goodButton, temp, randRow, randCol);
                goodButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        {
                            Animation FadeAnim = new AlphaAnimation(1.0f, 0.0f);//fade out the text
                            FadeAnim.setDuration(200);
                            playGood2(tapGood);                      // Play good swirl sound
                            GoodArray[finalI].TimerId.cancel();      // Cancel it's disappear Timer
                            GoodArray[finalI].ButtonId = null;       // Remove Button ID
                            GoodArray[finalI] = null;                // Remove from array
                            OnScreenGood--;
                            goodButton.setBackgroundResource(R.drawable.goodswirl_break); //Set image to goodswirl
                            v.startAnimation(FadeAnim);
                            v.setVisibility(View.INVISIBLE);         // Make Swirl disappear when clicked
                            v.setEnabled(false);                     // Disable button
                            if(DOUBLEPOINTS) {
                                Score+= 2;                                 // Add two to score
                            } else { Score++; }                            // Add one to score
                            Good_Pressed++;                                // Pressed One
                            Combo++;                                       // Add 1 to combo
                        }
                    }
                });
            }
        }
    } //End of displaybutton

    void Speed_Engine(int Score) {
        TextView SpeedPercent = (TextView)findViewById(R.id.SpeedLabel);
        if (( Good_Pressed + Good2_Pressed >= Speed_Increment) && Score != 0 && Current_Speed > Speed_Limit) //every (Speed_Increment) points, and the speed isn't going past the speed limit
        {
            Current_Speed -= Game_Speed_Add;           //Speed up the game by 50 frames/second
            Speed_Increment_Set += Speed_Increment_Add; // Set points to increment
            Speed_Increment += Speed_Increment_Set; //Add more points to increment to make it harder to speed up
            addTime = true;                         //Allows adding one add time button
            SwirlEngine.cancel();                   // cancel the old timer with the old speed
            SwirlEngine = new CountDownTimer(Current_Time, Current_Speed) { // start new timer with changed speed
                @Override
                public void onTick(long millisUntilFinished) {
                    if (millisUntilFinished == 0) {
                        onFinish();
                    } else {
                        displayButton();
                    }
                }
                @Override
                public void onFinish() {

                }
            }.start();
            int VeloPercent = ((100 / ((Start_Speed - Speed_Limit) / Game_Speed_Add)) * (((Start_Speed - Speed_Limit) / Game_Speed_Add) - ((Current_Speed - Speed_Limit) / Game_Speed_Add)));
            // Set the progress of the speedbar and Speed Label
            if(Current_Speed == Speed_Limit) {
                SpeedPercent.setText("Velocity Maxed!");
                Speed_Bar.setProgress(100);
                if(NukeActivated == false) {
                    Nuke.setImageResource(R.drawable.nuke_button_active); // Activate Nuke
                    Nuke.setEnabled(true);
                    Nuke.setClickable(true);
                    NukeActivated = true;
                }
                if(DoublingBomb.isEnabled()){
                    PointSpecial();
                }
                DOUBLEPOINTS = true;
            }
            else if (Current_Speed == Start_Speed) {
                SpeedPercent.setText("Velocity: 1%");
                Speed_Bar.setProgress(1);
            }
            else {
                SpeedPercent.setText("Velocity: " + VeloPercent + "%");
                Speed_Bar.setProgress(VeloPercent);
            }
            if(VeloPercent >= 50 && LightningBombActivated == false) {
                LightningBomb.setImageResource(R.drawable.lightning_button_active); // Activate LightningBomb
                LightningBomb.setEnabled(true);
                LightningBomb.setClickable(true);
                LightningBombActivated = true;
            }
            if(VeloPercent >= 25 && DoubleBombActivated == false) {
                DoublingBomb.setImageResource(R.drawable.double_button_active); // Activate LightningBomb
                DoublingBomb.setEnabled(true);
                DoublingBomb.setClickable(true);
                DoubleBombActivated = true;
            }
        } else {}
    } //End e

    public void DestroySwirls() {

        for (int i = 0; i < 10; i++) {
            if (GoodArray[i] != null) {
                GoodArray[i].TimerId.cancel();
                GoodArray[i].ButtonId.setVisibility(View.INVISIBLE);
                GoodArray[i].ButtonId.setEnabled(false);
            }
            if (BadArray[i] != null) {
                BadArray[i].TimerId.cancel();
                BadArray[i].ButtonId.setVisibility(View.INVISIBLE);
                BadArray[i].ButtonId.setEnabled(false);
            }
            if (SpecialArray[i] != null) {
                SpecialArray[i].TimerId.cancel();
                SpecialArray[i].ButtonId.setVisibility(View.INVISIBLE);
                SpecialArray[i].ButtonId.setEnabled(false);
            }
            GoodArray[i] = null;
            BadArray[i] = null;
            SpecialArray[i] = null;
        }
    }

    public void ExplodeNuke() {
        if(paused == false) {
            paused = true;
            Nuke.setEnabled(false);                 // Disable Nuke
            Nuke.setClickable(false);
            SwirlEngine.cancel();                   // Cancel All timers
            TimeCountdown.cancel();
            Updater.cancel();
            playSpecial(NukeBoom);                       // Play Nuke Explosion sound
            vibration.vibrate(1000);                 // Vibrate device for 500 milliseconds
            if(DOUBLEPOINTS) {
                Score += (OnScreenGood * 2);                  // Add All Scores and Time
                Score -= (OnScreenBad * 5);
                Score += (OnScreenGood2 * 4);
                Current_Time += (OnScreenTime * 5000);
                Good_Pressed += (OnScreenGood * 2);           // Add all the swirls that were pressed
                Good2_Pressed += (OnScreenGood2 * 2);
                Bad_Pressed += OnScreenBad;
                Time_Pressed += OnScreenTime;
            } else {
                Score += OnScreenGood;                  // Add All Scores and Time
                Score -= (OnScreenBad * 5);
                Score += (OnScreenGood2 * 2);
                Current_Time += (OnScreenTime * 5000);
                Good_Pressed += OnScreenGood;           // Add all the swirls that were pressed
                Good2_Pressed += OnScreenGood2;
                Bad_Pressed += OnScreenBad;
                Time_Pressed += OnScreenTime;
            }
            DestroySwirls();                        // Destroy the Swirls
            Nuke.setImageResource(R.drawable.nuke_button_inactive);
            new CountDownTimer(1000, 1000) { // Set timer for disappearance
                public void onTick(long millisUntilFinished) {
                    if (millisUntilFinished == 0) {
                        onFinish();
                    } else {
                    }
                }

                @Override
                public void onFinish() {
                    GameTimers(Current_Time);               // Start Timers
                    paused = false;
                }
            }.start();
        }
    }

    public void ThunderBolt() {
        int i;
        if (paused == false) {
            LightningBomb.setEnabled(false);                 // Disable Nuke
            LightningBomb.setClickable(false);
            paused = true;
            SwirlEngine.cancel();                   // Cancel All timers
            TimeCountdown.cancel();
            Updater.cancel();
            playSpecial(ThunderBoom);                       // Play Nuke Explosion sound
            vibration.vibrate(800);                 // Vibrate device for 500 milliseconds
            if(DOUBLEPOINTS) {
                Score += (OnScreenGood * 2);                  // Add All Scores and Time
                Score += (OnScreenGood2 * 4);
                Current_Time += (OnScreenTime * 5000);
                Good_Pressed += OnScreenGood * 2;           // Add all the swirls that were pressed
                Good2_Pressed += OnScreenGood2 * 2;
                Time_Pressed += OnScreenTime;
            } else {
                Score += OnScreenGood;                  // Add All Scores and Time
                Score += (OnScreenGood2 * 2);
                Current_Time += (OnScreenTime * 5000);
                Good_Pressed += OnScreenGood;           // Add all the swirls that were pressed
                Good2_Pressed += OnScreenGood2;
                Time_Pressed += OnScreenTime;
            }
            DestroySwirls();                        // Destroy the Swirls
            LightningBomb.setImageResource(R.drawable.lightning_button_inactive);
            new CountDownTimer(1000, 1000) { // Set timer for disappearance
                public void onTick(long millisUntilFinished) {
                    if (millisUntilFinished == 0) {
                        onFinish();
                    } else {
                    }
                }

                @Override
                public void onFinish() {
                    GameTimers(Current_Time);               // Start Timers
                    paused = false;
                }
            }.start();
        }
    }

    public void PointSpecial() {
        if(paused == false) {
            paused = true;
            DoublingBomb.setEnabled(false);                 // Disable Nuke
            DoublingBomb.setClickable(false);
            SwirlEngine.cancel();                   // Cancel All timers
            TimeCountdown.cancel();
            Updater.cancel();
            playSpecial(ThunderBoom);                       // Play Nuke Explosion sound
            vibration.vibrate(1000);                 // Vibrate device for 500 milliseconds
            Score += (OnScreenGood * 2);                  // Add All Scores and Time
            Score += (OnScreenGood2 * 4);
            Current_Time += (OnScreenTime * 5000);
            Good_Pressed += (OnScreenGood * 2);           // Add all the swirls that were pressed
            Good2_Pressed += (OnScreenGood2 * 2);
            Time_Pressed += OnScreenTime;
            DestroySwirls();                        // Destroy the Swirls
            DoublingBomb.setImageResource(R.drawable.double_button_inactive);
            new CountDownTimer(1000, 1000) { // Set timer for disappearance
                public void onTick(long millisUntilFinished) {
                    if (millisUntilFinished == 0) {
                        onFinish();
                    } else {
                    }
                }

                @Override
                public void onFinish() {
                    GameTimers(Current_Time);               // Start Timers
                    paused = false;
                }
            }.start();
        }
    }

    //Sound Players+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void playGood(Uri uri) {
        try{
            GoodSound.reset();
            GoodSound.setDataSource(this, uri);
            GoodSound.prepare();
            GoodSound.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void playGood2(Uri uri) {
        try{
            GoodSound2.reset();
            GoodSound2.setDataSource(this, uri);
            GoodSound2.prepare();
            GoodSound2.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void playBad(Uri uri) {
        try{
            BadSound.reset();
            BadSound.setDataSource(this, uri);
            BadSound.prepare();
            BadSound.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void playSpecial(Uri uri) {
        try{
            SpecialSound.reset();
            SpecialSound.setDataSource(this, uri);
            SpecialSound.prepare();
            SpecialSound.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Sound Players (end) ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    public void onClick(View v)
    {
        switch(v.getId()) {
            case R.id.pause_button:
                PauseActivate();                // Pause the game
                break;
            case R.id.Nuke:
                ExplodeNuke();
                break;
            case R.id.LightningButton:
                ThunderBolt();
                break;
            case R.id.DoubleButton:
                PointSpecial();
                break;
        }
    }

    //Pause Menu Options +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public void PauseActivate() {
        if(!paused) {
            paused = true;
            PauseButton.setEnabled(false);
            PauseButton.setClickable(false);
            gameBG.pause();                      // Pause the game music
            for (int i = 0; i < 10; i++) {        // cancel all the timers in disappear array
                if (GoodArray[i] != null) {
                    GoodArray[i].TimerId.cancel();
                }
                if (BadArray[i] != null) {
                    BadArray[i].TimerId.cancel();
                }
                if (SpecialArray[i] != null) {
                    SpecialArray[i].TimerId.cancel();
                }
            }
            SwirlEngine.cancel();               // cancel the rest of the timers
            Updater.cancel();
            TimeCountdown.cancel();
            PopupPauseMenu();                   //Popup the Pause menu
        }
    }

    //Continues game from Pause Menu
    public void Continue(View v) {
        new CountDownTimer(30,30) {
            int i;
            @Override
            public void onTick(long millisUntilFinished) {
                if (millisUntilFinished / 30 == 0)
                {
                    onFinish();
                }
                else
                {
                    for (i = 0; i < 10; i++) {        // starts all the timers in disappear array
                        if (GoodArray[i] != null) {
                            GoodArray[i].TimerId.start();
                        }
                        if (BadArray[i] != null) {
                            BadArray[i].TimerId.start();
                        }
                        if (SpecialArray[i] != null) {
                            SpecialArray[i].TimerId.start();
                        }
                    }
                }
            }

            @Override
            public void onFinish() {
                paused = false;
                popupWindow.dismiss();
                GameTimers(Current_Time);
                gameBG.start();
                PauseButton.setEnabled(true);
                PauseButton.setClickable(true);
            }
        }.start();
    }

    //Restarts game from Pause Menu
    public void Restart(View v) {
        popupWindow.dismiss();
        startActivity(new Intent(SinglePlayer.this, SinglePlayer.class));
        finish();
    }

    //Quits game from Pause Menu. Sends back to Title menu.
    public void Quit(View v) {
        popupWindow.dismiss();
        startActivity(new Intent(SinglePlayer.this, MainActivity.class));
        finish();
    }

    //Inflates Popup menu for Pause Button
    public void PopupPauseMenu() {
        try {
            LayoutInflater inflater = (LayoutInflater) SinglePlayer.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.activity_pause_menu, (ViewGroup)findViewById(R.id.pause_layout));
            popupWindow = new PopupWindow(layout, getWindow().getAttributes().width, getWindow().getAttributes().height, true);
            popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
            ImageButton Continue = (ImageButton)findViewById(R.id.Paused);
            Continue.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }});
            Button Continue2 = (Button)findViewById(R.id.Continue);
            Continue2.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }});
            Button Restart = (Button) findViewById(R.id.Restart);
            Restart.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }});
            Button Quit = (Button)findViewById(R.id.Quit);
            Quit.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }});
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    //PAUSE MENU ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    // Game Start and Game Over ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public void GameStart() {
        paused = true;
        Countdown_active = true;
        try {
            LayoutInflater inflater = (LayoutInflater) SinglePlayer.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.game_start_screen, (ViewGroup)findViewById(R.id.countdown_layout));
            popupWindow = new PopupWindow(layout, getWindow().getAttributes().width, getWindow().getAttributes().height, true);
            popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
        }catch(Exception e) {
            e.printStackTrace();
        }
        final TextView CountdownText = (TextView) popupWindow.getContentView().findViewById (R.id.count_down_text);

        CountdownTimer = new CountDownTimer(4000,1000) {
            public void onTick(long millisUntilFinished)
            {
                if (millisUntilFinished / 1000 == 0)
                {
                    onFinish();
                }
                else
                {
                    playSpecial(CountdownSound);                                  // Play sound
                    CountdownText.setText("" + millisUntilFinished / 1000 ); // Display Countdown
                }
            }
            public void onFinish()
            {
                playSpecial(CountdownGoSound);
                popupWindow.dismiss();      // Dismiss Countdown
                gameBG.start();             // Start background song
                GameTimers(StartTime);      // Start game timers
                paused = false;
                Countdown_active = false;
            }
        }.start();
    }

    public void GameOver() {
        paused = true;
        ScreenShot(SinglePlayer.this);  //Take screenshot of last second of game
        try {
            LayoutInflater inflater = (LayoutInflater) SinglePlayer.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.game_over_screen, (ViewGroup)findViewById(R.id.game_over_layout));
            popupWindow = new PopupWindow(layout, getWindow().getAttributes().width, getWindow().getAttributes().height, true);
            popupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
        }catch(Exception e) {
            e.printStackTrace();
        }
        //Wait x seconds before going to results screen
        new CountDownTimer(3000,3000) {
            int i = 0;
            public void onTick(long millisUntilFinished)
            {
                if (millisUntilFinished / 1000 == 0)
                {
                    onFinish();
                }
                else{
                    // Cancel the timers and clear the arrays
                    if (i != 10) {
                        for (i = 0; i < 10; i++) {
                            if (GoodArray[i] != null){
                                GoodArray[i].TimerId.cancel();
                            }
                            if (BadArray[i] != null){
                                BadArray[i].TimerId.cancel();
                            }
                            if (SpecialArray[i] != null){
                                SpecialArray[i].TimerId.cancel();
                            }
                            GoodArray[i] = null;
                            BadArray[i] = null;
                            SpecialArray[i] = null;
                        }
                    }
                }
            }

            @Override
            public void onFinish() {
                if(Combo > ComboMax) { ComboMax = Combo; }   // Get Max Combo
                TotalTapped();     //Add #swirls/#timeAdd to Total user counts
                spGamesPlayed();   //+1 game completed
                //* End the Game*\\
                popupWindow.dismiss();                                                //Dismiss Time's up popup
                gameBG.stop();                                                        //stop song
                Intent intentAgain = new Intent(SinglePlayer.this, PlayAgain.class);  //create intent (to go to PlayAgain menu)
                intentAgain.putExtra("score", Score);                                 //Send variable Score (score) to new intent (PlayAgain)
                intentAgain.putExtra("combo", ComboMax);                              //Send Max Combo
                intentAgain.putExtra("GoodSwirls", Good_Pressed);                     //Send number of GoodSwirls clicked
                intentAgain.putExtra("BadSwirls", Bad_Pressed);
                intentAgain.putExtra("Good2Swirls", Good2_Pressed);
                intentAgain.putExtra("TimeSwirls", Time_Pressed);
                startActivity(intentAgain);                                           //go to PlayAgain activity/menu
                finish();
            }
        }.start();
    }

    public void TotalTapped() {
        SharedPreferences prefsTotals = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editorTotals;
        NAME = prefsTotals.getString("PlayerName", "Player");

        //+++++++++GOOD SWIRL+++++++++++++++++++++++++++++
        if (prefsTotals.getInt("GoodSwirlTotal", 0) == 0) { //First game (or first green swirl tapped)
            editorTotals = prefsTotals.edit();
            editorTotals.putInt("GoodSwirlTotal", Good_Pressed);
            //editorTotals.putString("GoodSwirlTotalName", NAME);
            editorTotals.commit();
        } else if (prefsTotals.getInt("GoodSwirlTotal", 0) > 0) { //Add GoodSwirls clicked in this game to Total
            editorTotals = prefsTotals.edit();
            GoodSwirlTotalPass = prefsTotals.getInt("GoodSwirlTotal", 0);
            //NamePass = prefsTotals.getString("GoodSwirlTotal", "Player");
            GoodSwirlTotalPass = GoodSwirlTotalPass + Good_Pressed;
            editorTotals.putInt("GoodSwirlTotal", GoodSwirlTotalPass);
            //editorTotals.putString("GoodSwirlTotalName", NAME);
            editorTotals.commit();
        }
        //+++++++++TWICE SWIRL++++++++++++++++++++++++
        if (prefsTotals.getInt("TwiceSwirlTotal", 0) == 0) { //First game (or first yellow swirl tapped)
            editorTotals = prefsTotals.edit();
            editorTotals.putInt("TwiceSwirlTotal", Good2_Pressed);
            editorTotals.commit();
        } else if (prefsTotals.getInt("TwiceSwirlTotal", 0) > 0) { //Add TwiceSwirls clicked in this game to Total
            editorTotals = prefsTotals.edit();
            TwiceSwirlTotalPass = prefsTotals.getInt("TwiceSwirlTotal", 0);
            TwiceSwirlTotalPass = TwiceSwirlTotalPass + Good2_Pressed;
            editorTotals.putInt("TwiceSwirlTotal", TwiceSwirlTotalPass);
            editorTotals.commit();
        }
        //+++++++++BAD SWIRL++++++++++++++++++++++++
        if (prefsTotals.getInt("BadSwirlTotal", 0) == 0) { //First game (or first red swirl tapped)
            editorTotals = prefsTotals.edit();
            editorTotals.putInt("BadSwirlTotal", Bad_Pressed);
            editorTotals.commit();
        } else if (prefsTotals.getInt("BadSwirlTotal", 0) > 0) { //Add BadSwirls clicked in this game to Total
            editorTotals = prefsTotals.edit();
            BadSwirlTotalPass = prefsTotals.getInt("BadSwirlTotal", 0);
            BadSwirlTotalPass = BadSwirlTotalPass + Bad_Pressed;
            editorTotals.putInt("BadSwirlTotal", BadSwirlTotalPass);
            editorTotals.commit();
        }
        //+++++++++TIME ADD++++++++++++++++++++++++
        if (prefsTotals.getInt("TimeAddTotal", 0) == 0) { //First game (or first +5_Time tapped)
            editorTotals = prefsTotals.edit();
            editorTotals.putInt("TimeAddTotal", Time_Pressed);
            editorTotals.commit();
        } else if (prefsTotals.getInt("TimeAddTotal", 0) > 0) { //Add +5_Time clicked in this game to Total
            editorTotals = prefsTotals.edit();
            TimeAddTotalPass = prefsTotals.getInt("TimeAddTotal", 0);
            TimeAddTotalPass = TimeAddTotalPass + Time_Pressed;
            editorTotals.putInt("TimeAddTotal", TimeAddTotalPass);
            editorTotals.commit();
        }
    }//end TotalTapped()

    public void spGamesPlayed() {
        SharedPreferences prefsTotals = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editorTotals;
        NAME = prefsTotals.getString("PlayerName", "Player");
        if (prefsTotals.getInt("spGamesPlayedTotal", 0) == 0) { //First game played
            editorTotals = prefsTotals.edit();
            editorTotals.putInt("spGamesPlayedTotal", spFirstGamePlayed);
            editorTotals.commit();
        } else if (prefsTotals.getInt("spGamesPlayedTotal", 0) > 0) { //More than one game played, increment count +1
            editorTotals = prefsTotals.edit();
            SP_GamesPlayedTotalPass = prefsTotals.getInt("spGamesPlayedTotal", 0);
            SP_GamesPlayedTotalPass = SP_GamesPlayedTotalPass + 1;  //+1 SP game played
            editorTotals.putInt("spGamesPlayedTotal", SP_GamesPlayedTotalPass);
            editorTotals.commit();
        }//end 'else if'
    }//end spGamesPlayed()

    // Game Start and Game Over(end) ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    public void ScreenShot(Activity activity)
    {
        String screenshotPath = Environment.getExternalStorageDirectory().toString() + "/" + "screenshot.png";
        View view = getWindow().getDecorView().getRootView();
        view.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        OutputStream fout = null;
        File imageFile = new File(screenshotPath);
        try { //save bitmp as PNG file
            fout = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fout);
            fout.flush();
            fout.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void onResume() {
        super.onResume();

        GoodSound = new MediaPlayer();     // Setup MediaPlayer for good sound
        GoodSound2 = new MediaPlayer();    // Setup MediaPlayer for good sound
        BadSound = new MediaPlayer();      // Setup MediaPlayer for bad sound
        SpecialSound = new MediaPlayer();  // Setup MediaPlayer for Special button sounds
        gameBG = MediaPlayer.create(this, R.raw.game_song); //get background song
        gameBG.setLooping(true);    //make background song loop

        if (Countdown_active) { GameStart(); } // restart countdown timer
    }

    protected void onPause() {
        super.onPause();

        if (Countdown_active == false || !paused) { PauseActivate(); }                // Pause the game
        else { CountdownTimer.cancel(); popupWindow.dismiss(); }
        GoodSound.release();
        GoodSound2.release();
        BadSound.release();
        SpecialSound.release();
        gameBG.release();
    }

    @Override
    protected void onDestroy() {
        //moPubView.destroy();
        super.onDestroy();
        GoodSound.release();
        GoodSound2.release();
        BadSound.release();
        SpecialSound.release();
        gameBG.release();
    }

    public void onBackPressed() {
        PauseActivate();                // Pause the game
    }

}
