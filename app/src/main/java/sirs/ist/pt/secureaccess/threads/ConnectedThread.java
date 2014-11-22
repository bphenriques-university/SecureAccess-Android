package sirs.ist.pt.secureaccess.threads;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    SessionThread currentSession;

    public ConnectedThread(BluetoothSocket socket, SessionThread currentSession) {
        this.currentSession = currentSession;
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes = 0; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs

        while(true){
            try {
                // Read from the InputStream
                if(mmInStream.available() > 0) {
                    Log.i("CONNECTION", "Receiving from socket...");
                    bytes = mmInStream.read(buffer);
                    byte[] received = Arrays.copyOf(buffer, bytes);
                    currentSession.receiveData(received);
                }



                // Send the obtained bytes to the UI activity
                //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e("CONNECTION", "CANT RECEIVE BYTES");
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            Log.i("CONNECTION", "Writing to socket...");
            mmOutStream.write(bytes);
        } catch (IOException e) {
            Log.e("CONNECTION", "CANT SEND BYTES");
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}