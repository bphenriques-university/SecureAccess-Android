package sirs.ist.pt.secureaccess.threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.util.UUID;

import sirs.ist.pt.secureaccess.security.DiffieHellman;

/*
 * ConnectThread
 * connects the mobile to a given device and
 * starts another thread to take care of the connection
 * 
 */
public class SessionThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final UUID MY_UUID = UUID.fromString("841eba55-800a-48eb-9e39-335265d8d23f");

    ConnectedThread connectedThread;
    private boolean hasReceivedNewData = false;
    private byte[] newData;


    public SessionThread(BluetoothDevice device) {
        BluetoothSocket tmp = null;
        mmDevice = device;

        try {
            Log.i("CONN", "Trying to create socket to service with UUID: " + MY_UUID);
            ParcelUuid[] uuids = device.getUuids();


            boolean success = false;
            for (ParcelUuid u : uuids){
                String uuid = u.getUuid().toString();
                if(uuid.equalsIgnoreCase(MY_UUID.toString())){
                    logInfo("Server is available at that device");
                    success = true;
                    break;
                }
            }

            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);

        } catch (IOException e) {
            logInfo("Couldn't create socket");
        }
        mmSocket = tmp;

    }

    public void logInfo(String msg){
        Log.i("CONN", msg);
    }

    public void run() {
        mBluetoothAdapter.cancelDiscovery();

        try {
            logInfo("Trying to connect");
            mmSocket.connect();
            logInfo("Connected!");
        } catch (IOException connectException) {
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                logInfo("Couldn't close socket");
            }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        manageConnectedSocket(mmSocket);
    }

    private void manageConnectedSocket(BluetoothSocket mmSocket) {

        connectedThread = new ConnectedThread(mmSocket, this);
        connectedThread.start();

        mainCycle();
    }


    private void mainCycle() {
        //connected, trying to create keys
        DiffieHellman dh = new DiffieHellman();
        logInfo("Generating DH values");

        try {
            dh.createKeys();
        } catch (Exception e) {
            e.printStackTrace();
        }

        BigInteger x = dh.x_secret;
        BigInteger y = dh.y_device;
        BigInteger p = dh.p_prime;
        BigInteger g = dh.g_base;

        String send_public_values_DH = new String("CONN" + "#" + g.toString() + "#" + p.toString() + "#" + y.toString());

        write(send_public_values_DH.getBytes());

        //wait for kServer to generate values
        BigInteger serverY = parseYServer(new String(receive()));

        String session_key = dh.generateSessionKey(serverY, p, x);
        if (session_key == null){
            logInfo("BAD INPUT FROM SERVER IN OBTAINING YSERVER");
        }

        logInfo("Started sessiom with key: " + session_key);

        logInfo("End of main cycle");
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
        int counter = 0;
        while(counter != 10){
            //connected, trying to create keys

            Log.i("SESSION", "Sending msg number " + counter++);
            write(new String("Ping : SEQ = " + counter).getBytes());
            Log.i("SESSION", "SERVER: " + new String(receive()));
        }

        Log.i("SESSION", "End of Main Cycle");
    }


    public void write(byte[] bytes){
        connectedThread.write(bytes);
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
            mmSocket.close();
        } catch (IOException e) { }
    }
}