package com.example.spencer.swirlytap;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Random;


public class GameTest extends Activity {
    int count = 0; //this is total score

    private static final int NUM_ROWS = 8; //instantiated size of grid
    private static final int NUM_COLS = 6;
    Button buttons[][] = new Button[NUM_ROWS][NUM_COLS]; //created total number of grid buttons
    String[][] luckArray = new String[NUM_ROWS][NUM_COLS]; //array containing good and bad buttons
    protected boolean _active = true;
    protected int _gameEnd = 65000;  //after game ends, switch to 'PlayAgain' menu
    //temp change from 80000 to 45000 for testing purposes
    MediaPlayer gameBG; //for music

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // Removes Title Bar
        setContentView(R.layout.activity_single_player); //show res/layout/activity_single_player.xml
        gameBG = MediaPlayer.create(this, R.raw.game_song); //get song
//        gameBG.start(); //start song

        //Fill luck array with certain good and bad buttons
        Random rand = new Random(); //randomly select location in luck array

        for(int row = 0; row<NUM_ROWS; row++)
        {
            for(int col = 0; col<NUM_COLS; col++)
            {
                //bad button should display in a more random fashion...please test
                int randCell = rand.nextInt(NUM_COLS*NUM_ROWS); //random selection of numbers
                if(randCell%10 ==0) //much less chance to receive bad button
                {
                    luckArray[row][col] = "bad";
                }
                else
                    luckArray[row][col] = "good"; //much higher chance to receive good button
            }
        }

        // This Timer updates every 30 milliseconds, used for updating changing texts
        new CountDownTimer(60000, 30)
        {
            //
            //Get access to totalScore Textbox
            TextView totalScore= (TextView) findViewById(R.id.totalScore);

            public void onTick(long millisUntilFinished) {

                //Update totalScore Textbox with current score, end at 60 seconds
                if (millisUntilFinished / 30 == 0) {
                    onFinish();
                } else {
                    // Update Textfield
                    totalScore.setText("" + count);
                }
            }

            // Show text at end of timer
            public void onFinish() {

            }
        }.start();

        populateButtons(); //add buttons to grid


        new CountDownTimer(60000, 1000)
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
                    //display seconds left in text field
                    mTextField.setText("" + millisUntilFinished / 1000);
                //get a random number and modulo it to ROW and COL sizes
                //this gets random element in array
                displayButton(); //for now this is just every second
            }
            public void displayButton() //will call when button needs to be displayed
            {
                Random r = new Random(); //randomly select location in luck array
                int randRow = r.nextInt(NUM_ROWS);
                int randCol = r.nextInt(NUM_COLS);

                if(luckArray[randRow][randCol]=="good")
                {
                    Button goodButton = buttons[randRow][randCol];
                    goodButton.setBackgroundResource(R.drawable.goodswirl); //make this grid block location
                    //have the image of goodswirl
                    //Scale image to button: this makes all swirls small to fit grid block size
                    int newWidth = goodButton.getWidth();
                    int newHeight = goodButton.getHeight();
                    Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.goodswirl);
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
                    Resources resource = getResources();
                    goodButton.setBackground(new BitmapDrawable(resource, scaledBitmap));

                }
                else if(luckArray[randRow][randCol] == "bad")
                {//placed image of bill cosby as bad image until we can make a proper bad swirl. For the lols.
                    Button badButton = buttons[randRow][randCol];
                    badButton.setBackgroundResource(R.drawable.goodswirl); //make this grid block location
                    //have the image of goodswirl
                    //Scale image to button: this makes all swirls small to fit grid block size
                    int newWidth = badButton.getWidth();
                    int newHeight = badButton.getHeight();
                    Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cosby); //change cosby to low res image (less memory usage)
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
                    Resources resource = getResources();
                    badButton.setBackground(new BitmapDrawable(resource, scaledBitmap));

                }
                //could find other items such as 2x button

            }
            //stop time/game when time is up
            public void onFinish()
            {
                gameBG.stop(); //stop song
                mTextField.setText("0");
                //when game ends, the 'PlayAgain' menu is called
                Intent intentAgain = new Intent(GameTest.this, PlayAgain.class);  //create intent (to go to PlayAgain menu)
                intentAgain.putExtra("score", count);
                startActivity(intentAgain); //go to PlayAgain activity/menu
                finish();
            }
            //player clicks on swirl add point

        }.start();
    }


    private void populateButtons() //creating grid of buttons
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
                final int FINAL_COL = col; //set col and row location to pass to gridButton
                final int FINAL_ROW = row; //this sends location of button
                Button Swirl = new Button(this); //create button to display correctly
                Swirl.setBackgroundColor(Color.TRANSPARENT);
                Swirl.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT,1.0f));


                Swirl.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gridButtonClicked(FINAL_COL, FINAL_ROW); //send col and row clicked
                    }
                });
                tableRow.addView(Swirl);
                buttons[row][col] = Swirl;
            }
        }//end 'for'
    }//end private void populateButtons

    private void gridButtonClicked(int row, int col) //any time a button clicked do something
    {
        //determine what button is. if good then add point, if bad take away point, if 2x then 2 times points
        buttons[row][col].setBackgroundColor(Color.TRANSPARENT);
        count++;

    }

    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.pause_button:
                break;
        }
    }

}