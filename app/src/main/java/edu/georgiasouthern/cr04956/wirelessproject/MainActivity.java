package edu.georgiasouthern.cr04956.wirelessproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager.Channel channel;
    WifiP2pManager manager;
    MyReceiver receiver;
    boolean isWifiP2pEnabled = false;
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private MyPeerListener peerListListener = new MyPeerListener();
    private RecyclerView recycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //change in wifi p2p status
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        //change in list of available peers
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        //state of connectivity has changed
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        //device's details have changed
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);


        manager.discoverPeers(channel, new PeerDiscoveryListener(this));

        recycler = (RecyclerView) findViewById(R.id.devicesRecycler);
        recycler.setAdapter(new DeviceRecyclerAdapter());

//        connect();


    }

    private class DeviceOnClickListener implements View.OnClickListener {
        public WifiP2pDevice myDevice;

        public DeviceOnClickListener(WifiP2pDevice device) {
            myDevice = device;

        }

        @Override
        public void onClick(View v) {
            //ask if this is what you want, then uses device info
            connect(myDevice);
        }
    }

    private class DeviceViewHolder extends RecyclerView.ViewHolder {
        public TextView deviceInfoView;

        public View theView;

        public DeviceViewHolder(View itemView) {
            super(itemView);
            deviceInfoView = (TextView) itemView.findViewById(R.id.txtDeviceInfo);
            theView = itemView;
        }


    }

    private class DeviceRecyclerAdapter extends RecyclerView.Adapter<DeviceViewHolder> {

        @Override
        public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

//            TextView v = (TextView) LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.my_text_view, parent, false);
            LinearLayout item = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.device_list_item, parent, false);


            DeviceViewHolder holder = new DeviceViewHolder(item);


            return holder;
        }

        @Override
        public void onBindViewHolder(DeviceViewHolder holder, int position) {
            //peers.get(0)
            WifiP2pDevice dev = peers.get(position);
            DeviceOnClickListener listener = new DeviceOnClickListener(dev);
            holder.theView.setOnClickListener(listener);
            String devInfo = dev.deviceName + " (" + dev.deviceAddress + ")";
            holder.deviceInfoView.setText(devInfo);


        }

        @Override
        public int getItemCount() {
            return peers.size();
        }
    }
    //@Override
    public void connect(WifiP2pDevice device) {
        //edit to connect to specific ones
        //this could be entirely from the perspective of the client, i think
//        WifiP2pDevice device = peers.get(0);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        manager.connect(channel, config, new MyConnectionListener(this));
        //have connection listener do something
    }

    private class MyConnectionInfoListener implements WifiP2pManager.ConnectionInfoListener {

        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            InetAddress groupOwnerAddress = info.groupOwnerAddress;
            NetworkConnection connection = NetworkConnection.getInstance();

            if(info.groupFormed) {
                if(info.isGroupOwner) {
                    //set up incoming connections with server sockets. Pass to global?
                    connection.listenForConnection();
                } else {
                    //set up outgoing connection. host can send update to peer list and people make connections accordingly?
                    connection.establishConnection(groupOwnerAddress, null);//network connection listener
                }
            } else {
                //group not formed, something bad happened
                Toast.makeText(MainActivity.this,"Group not formed. Sorry, no idea what went wrong. Try again?",Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new MyReceiver(manager,channel,this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void setIsWifiP2pEnabled(boolean isEnabled) {
        isWifiP2pEnabled = isEnabled;
    }


    private class MyPeerListener implements WifiP2pManager.PeerListListener {

        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            List<WifiP2pDevice> refreshedPeers = (List<WifiP2pDevice>) peerList.getDeviceList();
            if(!refreshedPeers.equals(peers)) {
               peers.clear();
                peers.addAll(refreshedPeers);
                recycler.invalidate();
                //if adapter view is backed, notify of change

            }

            if(peers.size() == 0) {
//                Log.d(MainActivity.TAG, "No devices found");

                return;
            }
        }
    }

    private class MyReceiver extends BroadcastReceiver {

        MainActivity activity;
        WifiP2pManager manager;
        WifiP2pManager.Channel channel;

        public MyReceiver(WifiP2pManager sManager, WifiP2pManager.Channel sChannel, MainActivity act) {
            activity = act;
            channel = sChannel;
            manager = sManager;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                //determine if p2p is enabled and alert the activity
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    activity.setIsWifiP2pEnabled(true);
                } else {
                    activity.setIsWifiP2pEnabled(false);
                }
            } else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

                if(manager != null) {
                    manager.requestPeers(channel, peerListListener);
                }

            } else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                //connection state has changed
                if(manager == null) return;

                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if(networkInfo.isConnected()) {
                    //we are connected
                    manager.requestConnectionInfo(channel, new MyConnectionInfoListener());
                } else {
                    //not connected
                }

            } else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
//                DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
//                        .findFragmentById(R.id.frag_list);
//                fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
//                        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            }
        }
    }
}
