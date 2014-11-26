package sirs.ist.pt.secureaccess.threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import sirs.ist.pt.secureaccess.ItemDetailActivity;
import sirs.ist.pt.secureaccess.security.DiffieHellman;

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

            ParcelUuid[] uuids = device.getUuids();
            boolean success = false;
            for (ParcelUuid u : uuids){
                String uuid = u.getUuid().toString();
                if(uuid.equalsIgnoreCase(MY_UUID.toString())){
                    log("Server is available at that device");
                    success = true;
                    break;
                }
            }


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
        //connected, trying to create keys
        DiffieHellman dh = new DiffieHellman();
        log("Generating DH values");

        try {
            dh.createKeys();


            BigInteger x = dh.x_secret;
            BigInteger y = dh.y_device;
            BigInteger p = dh.p_prime;
            BigInteger g = dh.g_base;

            log("Sending DH values to server...");
            String send_public_values_DH = new String("CONN" + "#" + g.toString() + "#" + p.toString() + "#" + y.toString());
            write(send_public_values_DH.getBytes());

            //wait for kServer to generate values
            log("Waiting for server with his public keys...");
            BigInteger serverY = parseYServer(new String(receive()));

            String session_key = dh.generateSessionKey(serverY, p, x);
            if (session_key == null){
                log("BAD INPUT FROM SERVER IN OBTAINING YSERVER");
                return;
            }

            log("Started sessiom with key: " + session_key);

        } catch (Exception e) {
            log("Error in connection...");
            e.printStackTrace();
            return;
        }
        log("End of main cycle");
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

    private void pingPongTest() {
        try {
            int counter = 0;
            while (counter != 10) {
                //connected, trying to create keys

                Log.i("SESSION", "Sending msg number " + counter++);
                write(new String("Ping : SEQ = " + counter).getBytes());
                Log.i("SESSION", "SERVER: " + new String(receive()));
            }
        }catch(IOException e){
            log("Error connecting to socket");
        }

        Log.i("SESSION", "End of Main Cycle");
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