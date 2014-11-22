package sirs.ist.pt.secureaccess;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.Set;

import sirs.ist.pt.secureaccess.threads.SessionThread;

/**
 * Created by brunophenriques on 22/11/14.
 */
public class BluetoothManager {
    private BluetoothDevice selectedDevice;

    //Period of time in which the receiver is enabled
    private static final int REQUEST_ENABLE_BT = 12;
    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    private SessionThread currentSession;

    public void destroy(){
        if (btAdapter != null) {
            btAdapter.cancelDiscovery();
        }
    }

    public ArrayList<BluetoothDevice> getPairedDevices(){

        ArrayList<BluetoothDevice> result = new ArrayList<BluetoothDevice>();

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                result.add(device);
            }
        }

        return result;
    }

    public void connect(BluetoothDevice device){
        currentSession = new SessionThread(device);
        currentSession.start();
    }

    public BluetoothDevice getSelectedDevice() {
        return selectedDevice;
    }

    public void setSelectedDevice(BluetoothDevice selectedDevice) {
        this.selectedDevice = selectedDevice;
    }

    public void sendMessage(){
        currentSession.write(new String("Ping" + 10 * Math.random()).getBytes());
    }
}
