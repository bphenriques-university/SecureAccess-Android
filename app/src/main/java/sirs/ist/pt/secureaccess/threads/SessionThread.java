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
                Log.i("CONN", "Available uuid: " + uuid);
                if(uuid.equalsIgnoreCase(MY_UUID.toString())){
                    Log.i("CONN", "Server is available in that device");
                    success = true;
                    break;
                }
            }

            if(!success){
                Log.i("CONN", "Server is NOT available in that device");
            }

            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e("CONN", "Couldn't create socket");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("CONN", "Couldn't create socket: " + e.toString());
        }
        mmSocket = tmp;
    }

    public void run() {
        mBluetoothAdapter.cancelDiscovery();

        try {
            Log.i("CONN", "Trying to connect");
            mmSocket.connect();
            Log.i("CONN", "Connected!");
        } catch (IOException connectException) {
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e("CONN", "Couldn't close socket");
            }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        manageConnectedSocket(mmSocket);
    }

    private void manageConnectedSocket(BluetoothSocket mmSocket) {
        Log.i("CONN", "Creating connected Thread");

        connectedThread = new ConnectedThread(mmSocket, this);
        connectedThread.start();

        Log.i("SESSION", "Starting main cycle");
        mainCycle();
    }


    private void mainCycle() {
        //connected, trying to create keys
        DiffieHellman dh = new DiffieHellman();
        Log.i("SESSION", "Generating DH values ");

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
        String newData = new String(receive());

        BigInteger serverY = parseYServer(newData);

        String session_key = dh.generateSessionKey(serverY, p, x);
        if (session_key == null){
            Log.e("SESSION", "BAD INPUT FROM SERVER IN OBTAINING YSERVER");
        }
        Log.i("SESSION", "Started sessiom with key: " + session_key);

        Log.i("SESSION", "End of main cycle");
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