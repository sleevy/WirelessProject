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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        randy = new Random();
        userId = randy.nextLong();

        Button btnRoll = (Button) findViewById(R.id.btnRoll);
        final TextView outText = (TextView) findViewById(R.id.txtDiceRoll);
        btnRoll.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //roll dice
                int roll = rollDice(6);
                outText.setText(String.valueOf(roll));
                JSONObject json = makeObjectFromRoll(roll);

                NetworkConnection.getInstance().setListener(new MyConnectionListener());
                NetworkConnection.getInstance().broadcastData(json);

            }
        });


    }


    private class MyConnectionListener implements NetworkConnection.NetworkConnectionListener {

        @Override
        public void onReceiveData(JSONObject data) {
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
