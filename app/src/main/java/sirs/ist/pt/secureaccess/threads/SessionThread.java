package sirs.ist.pt.secureaccess.threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Base64;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import sirs.ist.pt.secureaccess.ItemDetailActivity;
import sirs.ist.pt.secureaccess.ListContent.DatabaseHandler;
import sirs.ist.pt.secureaccess.ListContent.Server;
import sirs.ist.pt.secureaccess.security.CipherText;

/*
 * ConnectThread
 * connects the mobile to a given device and
 * starts another thread to take care of the connection
 * 
 */
public class SessionThread extends Thread {
    public static final int TIMEOUT_SECONDS = 3;

    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final UUID MY_UUID = UUID.fromString("841eba55-800a-48eb-9e39-335265d8d23f");

    ConnectedThread connectedThread;
    private boolean hasReceivedNewData = false;
    private byte[] newData;

    ItemDetailActivity activity = null;


    public SessionThread(BluetoothDevice device, ItemDetailActivity activity) {
        BluetoothSocket tmp = null;
        mmDevice = device;
        this.activity = activity;

        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            log("Couldn't create socket");
        }
        mmSocket = tmp;
    }




    public void log(String msg){
        activity.log("SESSION:CONNECTION", msg);
    }

    public void run() {
        mBluetoothAdapter.cancelDiscovery();

        try {

            tryConnection();

            connectedThread = new ConnectedThread(mmSocket, this);
            connectedThread.start();

            mainCycle();
        } catch (ConnectException e){
            log("Error: " + e.getLocalizedMessage());
            activity.waitingForConnect = true;
        }catch (IOException connectException) {
            try {
                if(mmSocket != null) {
                    mmSocket.close();
                }
            }catch (IOException closeException) {
                log("Couldn't close socket");
            }
        }
    }

    private void tryConnection() throws ConnectException {

        boolean connected = false;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                log("Connecting...");
                mmSocket.connect();
                return "SUCCESS";
            }
        });

        try {
            String result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if(result.equals("SUCCESS")) {
                connected = true;
            }
        } catch (Exception e) {
            /* ignore error */
        }

        executor.shutdownNow();

        if (!connected) {
            throw new ConnectException("Couldn't establish connection");
        }

    }


    private void mainCycle() {
        try {
            Random random = new Random();

            String key = null;

            DatabaseHandler db = new DatabaseHandler(activity);
            Server s = db.getSharedKey(mmDevice.getAddress());
            if(s == null){
                log("trying to use server not known!");
            }else{
                log("SERVER KEY: " + s.getKey());
                key = s.getKey();
            }
            byte[] key_bytes = Base64.decode(key.getBytes(), Base64.DEFAULT);

            //REQUESTING CONNECTION

            int connection_challenge = random.nextInt(Integer.MAX_VALUE);
            String conn_request = "CONN#" + connection_challenge;
            log("[ME]: " + conn_request);
            write(CipherText.encrypt(conn_request, key_bytes).getBytes());

            //WAITING FOR SERVER REPLY
            log("Waiting for server response...");
            String connection_response = CipherText.decrypt(new String(receive()), key_bytes);
            log("[SERVER]: " + connection_response);

            String[] tokens = connection_response.split("#");
            byte[] session_key = null;
            int device_challenge = 0;

            //CONN_RESPONSE # KEY # CHALLENGE_RESPONSE # NEW_CHALLENGE
            if(tokens.length == 4 && tokens[0].equals("CONN_R")){
                session_key = Base64.decode(tokens[1], Base64.DEFAULT);
                int challenge_response = Integer.parseInt(tokens[2]);
                if(challenge_response != connection_challenge - 1){
                    throw new Exception("Server has failed this city");
                }
                device_challenge = Integer.parseInt(tokens[3]);
                device_challenge--;
            }else{
                throw new Exception("Bad server response");
            }

            String response = String.valueOf(device_challenge);
            log("[ME]: Session key from server: " + session_key);
            //answering server challenge
            write(CipherText.encrypt(response, session_key).getBytes());

            log("Started session with key: " + session_key);

            while(!isInterrupted()){

                //send heartbeat and challenge n1
                int alive_challenge = random.nextInt(Integer.MAX_VALUE);

                String alive_send = "ALIV#" + alive_challenge;
                log("[DEVICE]: " + alive_send);
                write(CipherText.encrypt(alive_send, session_key).getBytes());

                //EXPECTING ALIVE ANSWER with answer n1-1 and new challenge n2
                String ping_req = CipherText.decrypt(new String(receive()), session_key);
                log("[SERVER]: " + ping_req);

                int ping_number = -1;
                tokens = ping_req.split("#");
                if(tokens.length == 2){
                    int alive_challenge_response = Integer.parseInt(tokens[0]);
                    if (alive_challenge_response != (alive_challenge - 1)){
                        throw new Exception("Not the server i am talking too, replay attack");
                    }
                    ping_number = Integer.parseInt(tokens[1]);
                }else{
                    throw new Exception("Bad server response");
                }

                //ANSWERING SERVER PING
                String ping_response = Integer.toString(ping_number-1);
                log("[DEVICE]: " + ping_response);
                write(CipherText.encrypt(ping_response, session_key).getBytes());

                wait_for_ping_req();
            }

        } catch (Exception e) {
            log("Error during connection: " + e.getLocalizedMessage());
            e.printStackTrace();
            activity.waitingForConnect = true;
            return;
        }
        log("End of main cycle");
    }

    private void wait_for_ping_req() {
        try {
            log("Sleeping until check for ping-request");
            connectedThread.sleep(4000);
            log("woke up!");
        } catch (InterruptedException e) {
            e.printStackTrace();
            log("INTERRUPTED THREAD EXCEPTION");
        }
    }

    private BigInteger parseYServer(String newData) {
        String[] tokens = newData.split("#");

        BigInteger anotherY = null;
        if(tokens[0].equals("CONN-R") && tokens.length != 2){
            return null;
        }else{
            anotherY = new BigInteger(tokens[1]);
        }

        return anotherY;
    }

    public void write(byte[] bytes) throws IOException {
        if(connectedThread != null) {
            connectedThread.write(bytes);
        }
    }

    public byte[] receive(){
        while(!hasReceivedNewData){}
        hasReceivedNewData = false;

        return newData;
    }

    public void receiveData(byte[] received) {
        hasReceivedNewData = true;
        this.newData = received;
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {

        try {
            if(connectedThread != null){
                connectedThread.interrupt();
            }
            if(mmSocket != null) {
                mmSocket.close();
            }
        } catch (IOException e) {
            log("UPS, error closing socket!");
        }
    }
}