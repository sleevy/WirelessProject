package edu.georgiasouthern.cr04956.wirelessproject;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Cameron Rhodes on 4/23/2017.
 */

public class NetworkConnection {

    public static final int DEFAULT_PORT = 3333;
    public static final String JSON_FIELD_ACTION = "action";
    public static final String JSON_FIELD_DATA = "data";
    public static final String JSON_FIELD_USER = "user";
//    private long userId;

    private static NetworkConnection theInstance;
    //    private ArrayList<InetAddress> addresses;
//    private ArrayList<Socket> outgoing;
//    private ArrayList<ServerSocket> incoming;
    private ArrayList<NetworkAsyncTask> tasks;

    private NetworkConnection() {
//        Random rand = new Random();
//        userId = rand.nextLong();
//        addresses = new ArrayList<>();
//        outgoing = new ArrayList<>();
//        incoming = new ArrayList<>();
        tasks = new ArrayList<>();
    }

    public static NetworkConnection getInstance() {
        if(theInstance == null)
            theInstance = new NetworkConnection();
        return theInstance;
    }

//    public void addAddress(InetAddress newAddress) {
//        if(!addresses.contains(newAddress)) {
//            addresses.add(newAddress);
//        }
//    }
//
//    public void initializeConnections() {
//        //check if connection already initialized
//        //add a callback thing?
//    }


    public void establishConnection(final InetAddress address, NetworkConnectionListener listener) {
        Log.d("NETWORKCONN","Establish Connection");
        AsyncTask<String, Void, Void> makeSocket = new AsyncTask<String, Void, Void>() {
            NetworkAsyncTask t;
            @Override
            protected Void doInBackground(String... params) {

                try {
                    Socket socket = new Socket(address, DEFAULT_PORT);
                    socket.setKeepAlive(true);
                    NetworkAsyncTask task = new NetworkAsyncTask(socket);
                    tasks.add(task);
                    t = task;
//                    task.execute();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                t.execute();

            }
        };

        makeSocket.execute();
    }

    public void listenForConnection() {
        Log.d("NETWORKCONN","Listen for Connection");

        //set up server sockets asynchronously... eventually


        AsyncTask<String, Void, Void> makeSocket = new AsyncTask<String, Void, Void>() {
            NetworkAsyncTask t;
            @Override
            protected Void doInBackground(String... params) {

                try {
                    ServerSocket sock = new ServerSocket(DEFAULT_PORT);
                    Log.d("CONNECTION ASYNC", "BEFORE ACCEPT");
                    Socket connection = sock.accept();
                    Log.d("CONNECTION ASYNC", "AFTER ACCEPT");
                    connection.setKeepAlive(true);
                    NetworkAsyncTask task = new NetworkAsyncTask(connection);
                    tasks.add(task);
                    t = task;
//                    task.execute();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                t.execute();
                Log.d("NETWORK CONN ASYNC", "ON POST EXECUTE");
            }
        };

        makeSocket.execute();
    }

    public interface NetworkConnectionListener {
        //some method signature
        public void onReceiveData(JSONObject data);
    }

    public void broadcastData(JSONObject data) {
        for(NetworkAsyncTask t: tasks) {
            t.sendData(data);
        }

    }

    public void setListener(NetworkConnectionListener listener) {
        for(NetworkAsyncTask t: tasks) {
            t.setNetworkConnectionListener(listener);
        }
    }

    public int getNumberOfPeers() {
        return tasks.size();
    }

    public class NetworkAsyncTask extends AsyncTask<String, Void, Void> {
        private Socket theConnection;
        private Queue<JSONObject> dataQueue;
        private NetworkConnectionListener listen;

        public NetworkAsyncTask(Socket conn) {
            super();
            theConnection = conn;
            dataQueue = new ConcurrentLinkedQueue<>();
        }

        public void setNetworkConnectionListener(NetworkConnectionListener listener) {
            listen = listener;
        }

        @Override
        protected Void doInBackground(String[] params) {
            try {
                OutputStreamWriter out = new OutputStreamWriter(theConnection.getOutputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(theConnection.getInputStream()));

                while (theConnection.isConnected()) {
                    Log.d("NETWORK ASYNC", "Connection loop");
                    Log.d("CONNECTION ASYNC", "BEFORE WRITE");
                    JSONObject obj = dataQueue.poll(); //check null
                    if (obj != null) {
                        out.write(obj.toString());
                        out.flush();
                    }

                    StringBuilder readContents = new StringBuilder();
                    String line = "";
                    Log.d("CONNECTION ASYNC", "BEFORE READ");
                    if(theConnection.getInputStream().available() > 0) {


                        while ((line = reader.readLine()) != null) {
                            readContents.append(line).append("\n");
                        }
                        //handle contents by sending to listener
                        try {
                            JSONObject receiveJSON = new JSONObject(readContents.toString());
                            if (listen != null) {
                                listen.onReceiveData(receiveJSON);
                            } else {

                            }


                        } catch (JSONException jsone) {
                            jsone.printStackTrace();
                        }
                    }

                    Log.d("CONNECTION ASYNC", "AFTER READ");
                }


            }catch (IOException ioe) {
                ioe.printStackTrace();
            }
            return null;
        }

        public void closeConnection() {
            //send "this is closing" message? handle on other end?

            //close socket
        }

        public void sendData(JSONObject data) {
            dataQueue.offer(data);
        }
    }


}
