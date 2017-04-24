package edu.georgiasouthern.cr04956.wirelessproject;

import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

/**
 * Created by Cameron Rhodes on 4/23/2017.
 */
class MyConnectionListener implements WifiP2pManager.ActionListener {

    private MainActivity mainActivity;

    public MyConnectionListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onSuccess() {

        //not sure what to do here, maybe set up a socket and map it to an id?
        Toast.makeText(mainActivity, "Connection successful", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailure(int reason) {
        Toast.makeText(mainActivity, "Connection failed. Retry.", Toast.LENGTH_SHORT).show();

    }
}
