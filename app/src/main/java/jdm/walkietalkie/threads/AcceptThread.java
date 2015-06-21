package jdm.walkietalkie.threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.util.UUID;
import jdm.walkietalkie.MainActivity;
import jdm.walkietalkie.Util.HandlerCases;

public class AcceptThread extends Thread {
    private final UUID APP_UUID;
    private final BluetoothServerSocket mmServerSocket;
    private final String NAME = "Server";
    private final BluetoothAdapter mBluetoothAdapter;
    private final int SUCCESS_CONNECT = HandlerCases.SUCCESS_CONNECT;

    public AcceptThread(BluetoothAdapter adapter,UUID uuid) {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        this.mBluetoothAdapter = adapter;
        this.APP_UUID = uuid;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, APP_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    //Notify the MainActivity handler that the connection was successful
                    MainActivity.getHandler().obtainMessage(SUCCESS_CONNECT, socket).sendToTarget();
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

}
