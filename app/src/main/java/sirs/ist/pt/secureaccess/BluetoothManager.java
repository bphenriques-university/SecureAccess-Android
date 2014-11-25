package sirs.ist.pt.secureaccess;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.widget.TextView;

import sirs.ist.pt.secureaccess.threads.SessionThread;

public class BluetoothManager {
    public static BluetoothDevice selectedDevice;

    //Period of time in which the receiver is enabled
    private static final int REQUEST_ENABLE_BT = 12;
    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    private static SessionThread currentSession;
    private static TextView textview = null;

    public void destroy(){
        if (btAdapter != null) {
            btAdapter.cancelDiscovery();
        }
    }



    public static void connect(){
        try {
            Log.i("CONN", "Creating sessionThread...");
            currentSession = new SessionThread(selectedDevice);
            Log.i("CONN", "Starting connection...");
            currentSession.start();
        } catch (Exception e) {
            Log.i("CONN", "Couldn't stablish connection. Reason: " + e.toString());
            e.printStackTrace();
        }
    }

    public static void disconnect(){
        Log.i("CONN", "Disconnecting...");
        currentSession.cancel();
    }


    public static void setSelectedDevice(BluetoothDevice device) {
        selectedDevice = device;
    }

    public void sendMessage(){
        currentSession.write(new String("Ping" + 10 * Math.random()).getBytes());
    }
}
