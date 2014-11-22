package sirs.ist.pt.secureaccess.threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

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
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e("CONN", "Couldn't create socket");
        }
        mmSocket = tmp;
    }

    public void run() {
        mBluetoothAdapter.cancelDiscovery();

        try {
            Log.i("CONN", "Connecting");
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

    public void write(byte[] bytes){
        connectedThread.write(bytes);
    }

    private void mainCycle() {
        int counter = 0;
        while(counter != 10){
            Log.i("SESSION", "Sending msg number " + counter++);
            write(new String("Ping : SEQ = " + counter).getBytes());
            Log.i("SESSION", "SERVER: " + new String(receive()));
        }

        Log.i("SESSION", "End of Main Cycle");

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