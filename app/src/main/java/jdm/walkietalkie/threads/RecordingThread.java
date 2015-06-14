package jdm.walkietalkie.threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

/**
 * Created by tmast_000 on 6/13/2015.
 */
public class RecordingThread extends Thread {
    private ConnectedThread connectedThread;

    public RecordingThread(ConnectedThread connectedThread) {
        this.connectedThread = connectedThread;
    }

        public void run() {
            // Recieve audio from mic and send to connected Thread as bytes
            String toWrite = "Hello !!!";
            connectedThread.write(toWrite.getBytes());
        }

}
