package edu.georgiasouthern.cr04956.wirelessproject;

import android.net.Network;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class GameActivity extends AppCompatActivity {

    Random randy;
    public static final String ACTION_SEND_DICE_ROLL = "Dice roll";
    private long userId;
    int old;
    String player, ran, played, player2, player3;
    TextView outText, oldRoll, runner, runner2, runner3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        randy = new Random();
        userId = randy.nextLong();

        Button btnRoll = (Button) findViewById(R.id.btnRoll);
        outText = (TextView) findViewById(R.id.txtDiceRoll);
        oldRoll = (TextView) findViewById(R.id.textView4);
        runner = (TextView) findViewById(R.id.textView2);
        runner2 = (TextView) findViewById(R.id.textView3);
        runner3 = (TextView) findViewById(R.id.textView5);

        player = (String) runner.getText();
        player2 = (String) runner2.getText();
        player3 = (String) runner3.getText();
        ran = "";


        old = 0;
        btnRoll.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //roll dice
                oldRoll.setText("Previous roll: " + old);

                int roll = rollDice(6);
                outText.setText("Dice roll: " + String.valueOf(roll));
                JSONObject json = makeObjectFromRoll(roll);

                //Previous roll
                old = roll;

                switch(roll)
                {
                    case 1:
                    {
                        ran = "X" + ran;
                        played = ran + player;
                        runner.setText(played);
                        break;

                    }
                    case 2:
                    {
                        ran = "XX" + ran;
                        played = ran + player;
                        runner.setText(played);
                        break;

                    }
                    case 3:
                    {
                        ran = "XXX" + ran;
                        played = ran + player;
                        runner.setText(played);
                        break;
                    }
                    case 4:
                    {
                        ran = "XXX" + ran;
                        played = ran + player;
                        runner.setText(played);
                        break;
                    }
                    case 5:
                    {
                        ran = "XXXXX" + ran;
                        played = ran + player;
                        runner.setText(played);
                        break;
                    }
                    case 6:
                    {
                        ran = "XXXXXX" + ran;
                        played = ran + player;
                        runner.setText(played);
                        break;
                    }
                }

                NetworkConnection.getInstance().setListener(new MyConnectionListener());
                NetworkConnection.getInstance().broadcastData(json);

            }
        });


    }


    private class MyConnectionListener implements NetworkConnection.NetworkConnectionListener {

        @Override
        public void onReceiveData(JSONObject data)
        {
            Toast.makeText(GameActivity.this,"Received data" , Toast.LENGTH_SHORT).show();
            //probably isn't in UI thread, so may result in errors. push data into some sort of construct, maybe?
            Toast.makeText(GameActivity.this, data.toString(), Toast.LENGTH_LONG).show();
            //set up asynctask with onPostExecute?
        }
    }

    public int rollDice(int maxRollValue) {
        return randy.nextInt(maxRollValue) + 1;
    }

    public JSONObject makeObjectFromRoll(int value) {
        JSONObject json = new JSONObject();
        try {

            json.put(NetworkConnection.JSON_FIELD_ACTION, ACTION_SEND_DICE_ROLL);
            json.put(NetworkConnection.JSON_FIELD_DATA, value);
            json.put(NetworkConnection.JSON_FIELD_USER, userId);

        } catch (JSONException jsone) {
            jsone.printStackTrace();
        }

        return json;
    }


}
