package com.example.spencer.swirlytap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;
import java.util.Random;

public class SinglePlayer extends Activity implements View.OnClickListener {
    int Score = 0; //this is total score
    boolean addTime = false;    //Allows Time button to appear
    boolean Pause_Active = false;     //True if game is paused
    PopupWindow PauseWindow;    //Popup Window for Pause Menu
    PopupWindow CountdownWindow;//Popup Window for Countdown at the beginning of the game
    //Grid & Storage ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    private static final int NUM_ROWS = 6; //instantiated size of grid
    private static final int NUM_COLS = 4;
    private static final int ARRAY_ROWS = NUM_ROWS * 5;
    private static final int ARRAY_COLS = NUM_COLS * 5;
    private static int FINAL_COL; //set col and row location to pass to gridButton
    private static int FINAL_ROW; //this sends location of button
    Button buttons[][] = new Button[NUM_ROWS][NUM_COLS];   //created total number of grid buttons
    String[][] luckArray = new String[ARRAY_ROWS][ARRAY_COLS]; //array containing good and bad buttons
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    //Time & Speed ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    int StartTime = 61000;       //Set start time, 60000 = 60 seconds temporary set to 20 seconds
    int Current_Time = 61000;    //Current in-game time
    int Game_Speed = 400;        //Speed of the game
    int Speed_Limit = 200;       //Highest Speed
    int Game_Speed_Add = 15;     //Add speed every increment
    int Speed_Increment = 8;     //Points needed to increment speed
    int Speed_Increment_Add = 4; //Add to Speed_Increment to make it harder to speed up
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    ImageButton PauseButton; //create image button for pause
    //Music & Sounds ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    MediaPlayer gameBG;       // For Background music
    MediaPlayer CountdownSound; //Sound for CountDown
    Uri tapGood;              // Sound when Good swirl is tapped
    Uri tapBad;               // Sound for Bad swirl
    Uri tapTimeAdd;           // Sound for Time add swirl
    MediaPlayer GoodSound;    // MediaPlayer for playing good sound
    MediaPlayer GoodSound2;   // MediaPlayer for playing good sound - for faster sound
    MediaPlayer BadSound;     // MediaPlayer for playing Bad sound
    MediaPlayer SpecialSound; // MediaPlayer for playing Special button sounds
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++//
    CountDownTimer Updater;        //Timer that updates test and values at 30 frames/ms(millisecond)
    CountDownTimer TimeCountdown;  //Timer that updates the timer every second
    CountDownTimer SwirlEngine;    //Timer that lets the swirls appear. Update depends on Game_Speed
    CountDownTimer CountdownTimer; //Timer for the countdown at the beginning

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // Removes Title Bar
        setContentView(R.layout.activity_single_player); //show res/layout/activity_single_player.xml
        gameBG = MediaPlayer.create(this, R.raw.game_song); //get background song
        CountdownSound = MediaPlayer.create(this, R.raw.countdown); //get countdown sound
        PauseButton= (ImageButton)findViewById(R.id.pause_button);
        PauseButton.setOnClickListener(this); //sets an onClickListener on PauseButton
        gameBG.setLooping(true);    //make background song loop
        //tapGood = MediaPlayer.create(this, R.raw.tap_good); //get sound for a tap on a good swirl
        GoodSound = new MediaPlayer();     // Setup MediaPlayer for good sound
        GoodSound2 = new MediaPlayer();    // Setup MediaPlayer for good sound
        BadSound = new MediaPlayer();      // Setup MediaPlayer for bad sound
        SpecialSound = new MediaPlayer();  // Setup MediaPlayer for Special button sounds
        tapGood = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.tap_good);    //Setup sound for Good swirl
        tapBad = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.tap_bad);      //Setup sound for Bad swirl
        tapTimeAdd = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.tap_time); //Setup sound for Time up swirl
        //tapBad = MediaPlayer.create(this, R.raw.tap_bad);   //get sound for a tap on a bad swirl
        //tapTimeAdd = MediaPlayer.create(this, R.raw.tap_time);   //get sound for a tap on a time swirl
        populateButtons(); //add buttons to grid

        ///////////////////////////////////////POPULATE LUCK ARRAY///////////////////////////////////

        //POPULATE luck array with different types of buttons
        Random rand = new Random(); //randomly select location in luck array

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
                else if(randCell%21 == 0)
                {
                    luckArray[row][col] = "addTime"; //place add time button in array
                }
                else
                    luckArray[row][col] = "good"; //much higher chance to receive good button
            }
        }
        /////////////////////////////////////////////////////////////////////////////////////////

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
                GameStartCountdown();       //Start beginning Countdown
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

    private Animation disabledAnimation() {
        Animation disabledAnim = new AlphaAnimation(1.0f, 0.0f);
        disabledAnim.setDuration(200);
        disabledAnim.setFillEnabled(true);
        disabledAnim.setFillAfter(true);
        disabledAnim.cancel();
        return disabledAnim;
    }

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
                    Current_Time = (int) millisUntilFinished;             //Updates current time
                mTextField.setText("" + millisUntilFinished / 1000); //display seconds left in text field
            }
            //stop time/game when time is up
            public void onFinish()
            {
                gameBG.stop();                                                        //stop song
                mTextField.setText("0");                                              //Set end of timer
                Intent intentAgain = new Intent(SinglePlayer.this, PlayAgain.class);  //create intent (to go to PlayAgain menu)
                intentAgain.putExtra("score", Score);                                 //Send variable Score (score) to new intent (PlayAgain)
                startActivity(intentAgain);                                           //go to PlayAgain activity/menu
                finish();
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
                    Speed_Engine(Score);           //Update the Speed
                }
            }
            // Show text at end of timer
            public void onFinish() {
            }
        }.start();

        SwirlEngine = new CountDownTimer(Time, Game_Speed) {
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

    }

    public void displayButton() //will call when button needs to be displayed
    {
        final Runnable buttonRunnable;
        final Handler buttonHandler = new Handler();
        Random r = new Random(); //randomly select location in luck array
        int randRow = r.nextInt(NUM_ROWS);
        int randCol = r.nextInt(NUM_COLS);

        if(luckArray[randRow][randCol]=="good") //GOOD BUTTON +1 POINT IF CLICKED
        {
            final Button goodButton = buttons[randRow][randCol];     //Button in this location
            goodButton.setBackgroundResource(R.drawable.goodswirl); //Set image to goodswirl
            goodButton.setEnabled(true);                            //Enable Swirl
            goodButton.setVisibility(View.VISIBLE);                 //Make Swirl Visible
            buttonRunnable = new Runnable() { //what will be called if button has not been clicked
                public void run()
                {

                    goodButton.setVisibility(View.INVISIBLE);
                    goodButton.setEnabled(false);

                }
            };
            if(goodButton.isEnabled() == true) {
                buttonHandler.postDelayed(buttonRunnable, 1600); //will disappear after 2 seconds
            }
            goodButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v)
                {

                    {
                        //tapGood.start();                         // Play short confirmation sound
                        playGood(tapGood);
                        //v.startAnimation(disabledAnimation());
                        v.setVisibility(View.INVISIBLE);         // Make Swirl disappear when clicked
                        v.setEnabled(false);                     // Disable button
                        Score++;                                 // Add one to score
                    }
                }
            });


            //set 1 second timer... if timer reached then make button disappear
        }
        else if(luckArray[randRow][randCol] == "bad")
    {
        final Button badButton = buttons[randRow][randCol];     //Button in this location
        badButton.setBackgroundResource(R.drawable.badswirl); //Set image to goodswirl
        badButton.setEnabled(true);                            //Enable Swirl
        badButton.setVisibility(View.VISIBLE);                 //Make Swirl Visible
        buttonRunnable = new Runnable() { //what will be called if button has not been clicked
            public void run()
            {

                badButton.setVisibility(View.INVISIBLE);
                badButton.setEnabled(false);

            }
        };
        if(badButton.isEnabled() == true) {
            buttonHandler.postDelayed(buttonRunnable, 1700); //will disappear after 2 seconds
        }
        badButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {

                {
                    //tapBad.start();                         // Play short confirmation sound
                    playBad(tapBad);
                    //v.startAnimation(disabledAnimation());
                    v.setVisibility(View.INVISIBLE);         // Make Swirl disappear when clicked
                    v.setEnabled(false);                     // Disable button
                    Score -= 5;                                 // Add one to score
            }
            }
        });

    }
    else if(luckArray[randRow][randCol] == "twicePoints")
    {
        final Button twiceButton = buttons[randRow][randCol];     //Button in this location
        twiceButton.setBackgroundResource(R.drawable.twiceswirl); //Set image to goodswirl
        twiceButton.setEnabled(true);                            //Enable Swirl
        twiceButton.setVisibility(View.VISIBLE);                 //Make Swirl Visible
        buttonRunnable = new Runnable() { //what will be called if button has not been clicked
            public void run()
            {

                twiceButton.setVisibility(View.INVISIBLE);
                twiceButton.setEnabled(false);

            }
        };
        if(twiceButton.isEnabled() == true) {
            buttonHandler.postDelayed(buttonRunnable, 1600); //will disappear after 2 seconds
        }
        twiceButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {

                {
                    //tapGood.start();                         // Play short confirmation sound
                    playGood2(tapGood);
                    //v.startAnimation(disabledAnimation());
                    v.setVisibility(View.INVISIBLE);         // Make Swirl disappear when clicked
                    v.setEnabled(false);                     // Disable button
                    Score+=2;                                 // Add one to score
                }
            }
        });

    }
    else if(luckArray[randRow][randCol] == "addTime") {
            if (addTime == true) {
                final Button timeButton = buttons[randRow][randCol];     //Button in this location
                timeButton.setBackgroundResource(R.drawable.fivetime); //Set image to goodswirl
                timeButton.setEnabled(true);                            //Enable Swirl
                timeButton.setVisibility(View.VISIBLE);                 //Make Swirl Visible
                buttonRunnable = new Runnable() { //what will be called if button has not been clicked
                    public void run() {

                        timeButton.setVisibility(View.INVISIBLE);
                        timeButton.setEnabled(false);

                    }
                };
                if (timeButton.isEnabled() == true) {
                    buttonHandler.postDelayed(buttonRunnable, 1800); //will disappear after 2 seconds
                }
                timeButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        {
                            addTime = false;                        // Stop more time buttons from popping up
                            //tapTimeAdd.start();                        // Play short confirmation sound
                            playSpecial(tapTimeAdd);
                            v.setVisibility(View.INVISIBLE);        // Make Swirl disappear when clicked
                            v.setEnabled(false);                    // Disable button
                            SwirlEngine.cancel();                   // Cancel old Timers
                            Updater.cancel();
                            TimeCountdown.cancel();
                            Current_Time += 5000;                             // Add 5 seconds to time
                            GameTimers(Current_Time);                          // Add one to score
                        }
                    }
                });

            } else if (addTime == false) {
                final Button goodButton = buttons[randRow][randCol];     //Button in this location
                goodButton.setBackgroundResource(R.drawable.goodswirl); //Set image to goodswirl
                goodButton.setEnabled(true);                            //Enable Swirl
                goodButton.setVisibility(View.VISIBLE);                 //Make Swirl Visible
                buttonRunnable = new Runnable() { //what will be called if button has not been clicked
                    public void run() {

                        goodButton.setVisibility(View.INVISIBLE);
                        goodButton.setEnabled(false);

                    }
                };
                if (goodButton.isEnabled() == true) {
                    buttonHandler.postDelayed(buttonRunnable, 1500); //will disappear after 2 seconds
                }
                goodButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        {
                            //tapGood.start();                         // Play short confirmation sound
                            playGood2(tapGood);
                            //v.startAnimation(disabledAnimation());
                            v.setVisibility(View.INVISIBLE);         // Make Swirl disappear when clicked
                            v.setEnabled(false);                     // Disable button
                            Score++;                                 // Add one to score
                        }
                    }
                });

            }
        }
    } //End of displaybutton

    void Speed_Engine(int Score) {

        if ((Score % Speed_Increment) == 0 && Score != 0 && Game_Speed >= Speed_Limit) //every (Speed_Increment) points, and the speed isn't going past the speed limit
        {
            Game_Speed -= Game_Speed_Add;           //Speed up the game by 50 frames/second
            Speed_Increment += Speed_Increment_Add; //Add more points to increment to make it harder to speed up
            addTime = true;                         //Allows adding one add time button
            SwirlEngine.cancel();                   // cancel the old timer with the old speed
            SwirlEngine = new CountDownTimer(Current_Time, Game_Speed) { // start new timer with changed speed
                @Override
                public void onTick(long millisUntilFinished) {
                    if (millisUntilFinished / 30 == 0) {
                        onFinish();
                    } else {
                        displayButton();
                    }
                }
                @Override
                public void onFinish() {

                }
            }.start();
        } else {
        }
    } //End Speed_Engine

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
    //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    public void onClick(View v)
    {
        switch(v.getId()) {
            case R.id.pause_button:
                Pause_Active = true;
                gameBG.pause();
                SwirlEngine.cancel();
                Updater.cancel();
                TimeCountdown.cancel();
                PopupPauseMenu();
            break;
        }
    }

    //Pause Menu Options +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    //Continues game from Pause Menu
    public void Continue(View v) {
        Pause_Active = false;
        PauseWindow.dismiss();
        GameTimers(Current_Time);
        gameBG.start();
    }

    //Restarts game from Pause Menu
    public void Restart(View v) {
        Pause_Active = false;
        PauseWindow.dismiss();
        startActivity(new Intent(SinglePlayer.this, SinglePlayer.class));
        finish();
    }

    //Quits game from Pause Menu. Sends back to Title menu.
    public void Quit(View v) {
        Pause_Active = false;
        PauseWindow.dismiss();
        startActivity(new Intent(SinglePlayer.this, MainActivity.class));
        finish();
    }

    //Inflates Popup menu for Pause Button
    public void PopupPauseMenu() {
        try {
            LayoutInflater inflater = (LayoutInflater) SinglePlayer.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.activity_pause_menu, (ViewGroup)findViewById(R.id.pause_layout));
            PauseWindow = new PopupWindow(layout, 380, 800, true);
            PauseWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
            ImageButton Continue = (ImageButton)findViewById(R.id.Paused);
            Continue.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PauseWindow.dismiss();
                }});
            Button Continue2 = (Button)findViewById(R.id.Continue);
            Continue2.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PauseWindow.dismiss();
                }});
            Button Restart = (Button) findViewById(R.id.Restart);
            Restart.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PauseWindow.dismiss();
                }});
            Button Quit = (Button)findViewById(R.id.Quit);
            Quit.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PauseWindow.dismiss();
                }});
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void GameStartCountdown() {
        try {
            LayoutInflater inflater = (LayoutInflater) SinglePlayer.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.game_begin_countdown, (ViewGroup)findViewById(R.id.countdown_layout));
            CountdownWindow = new PopupWindow(layout, 380, 800, true);
            CountdownWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
        }catch(Exception e) {
            e.printStackTrace();
        }

        CountdownTimer = new CountDownTimer(4000,1000) {

            TextView CountdownText = (TextView)CountdownWindow.getContentView().findViewById (R.id.count_down_text);

            public void onTick(long millisUntilFinished)
            {
                if (millisUntilFinished / 1000 == 0)
                {
                    onFinish();
                }
                else
                {
                    CountdownSound.start();
                    CountdownText.setText("" + millisUntilFinished / 1000 ); //display seconds left in text field
                }
            }
            public void onFinish()
            {
                CountdownWindow.dismiss();
                gameBG.start();             //start background song
                GameTimers(StartTime);      //start game timers
            }

        }.start();

    }


}
