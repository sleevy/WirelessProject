package edu.georgiasouthern.cr04956.wirelessproject;

import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

/**
 * Created by Cameron Rhodes on 4/23/2017.
 */
class PeerDiscoveryListener implements WifiP2pManager.ActionListener {

    private MainActivity mainActivity;

    public PeerDiscoveryListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onSuccess() {
        Toast.makeText(mainActivity.getApplicationContext(), "Successful discovery!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailure(int reason) {
        Toast.makeText(mainActivity.getApplicationContext(), "Discovery failed", Toast.LENGTH_SHORT).show();
    }
}
