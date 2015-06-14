package jdm.walkietalkie.threads;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.util.UUID;
import jdm.walkietalkie.MainActivity;

/**
 * Created by tmast_000 on 6/13/2015.
 */
public class ConnectPhoneThread extends Thread {
    private BluetoothAdapter mBluetoothAdapter;
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private UUID APP_UUID;

    private final int SUCCESS_CONNECT = 0;

    public ConnectPhoneThread(BluetoothDevice device,BluetoothAdapter adapter,UUID uuid) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            this.mBluetoothAdapter = adapter;
            this.mmDevice = device;
            this. APP_UUID = uuid;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(APP_UUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            MainActivity.getHandler().obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
        }

    /**
     * Will cancel an in-progress connection, and close the socket
     */

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
        }
    }
}
