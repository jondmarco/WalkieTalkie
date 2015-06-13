package jdm.walkietalkie;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity implements ListView.OnClickListener, ListView.OnItemClickListener {

    ListView lvPaired, lvDiscovered;
    Button bScan, bDiscover;
    ArrayAdapter<String> pAdapter, dAdapter;
    BluetoothAdapter mBluetoothAdapter;
    private final UUID APP_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    ConnectPhoneThread connectPhoneThread;
    AcceptThread acceptThread;
    ConnectedThread connectedThread;
    private final int SUCCESS_CONNECT = 0;
    private final int SEND_MIC_AUDIO = 1;
    private AudioRecord recorder = null;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SUCCESS_CONNECT:
                    connectedThread = new ConnectedThread(
                            (BluetoothSocket) msg.obj);
                    connectedThread.start();
                    break;
                case SEND_MIC_AUDIO:


                    connectedThread.write((byte[]) msg.obj);
                    break;
            }
        }
    };

    protected Context activityContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityContext = this;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        setContentView(R.layout.activity_main);
        setupViews();
        showPairedDevices();
        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    protected void setupViews() {
        lvPaired = (ListView) findViewById(R.id.lvPaired);
        lvDiscovered = (ListView) findViewById(R.id.lvDiscovered);
        lvDiscovered.setOnItemClickListener(this);
        bScan = (Button) findViewById(R.id.bScan);
        bScan.setOnClickListener(this);
        bDiscover = (Button) findViewById(R.id.bDiscover);
        bDiscover.setOnClickListener(this);
    }

    protected void showPairedDevices() {
        pAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getPairedDevices());
        lvPaired.setAdapter(pAdapter);
    }

    protected void showDiscoveredDevices() {
        dAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        discoverDevices();
        lvDiscovered.setAdapter(dAdapter);
    }

    protected String[] getPairedDevices() {

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        String[] returnString = new String[10];
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            int index = 0;
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                returnString[index] = device.getName() + ": " + device.getAddress();
                index++;
            }
        }

        return returnString;
    }

    protected void discoverDevices() {
        // Create a BroadcastReceiver for ACTION_FOUND
        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    dAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        };
// Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        return;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.bScan:
                showDiscoveredDevices();
                mBluetoothAdapter.startDiscovery();
                break;
            case R.id.bDiscover:

                break;
            default:

                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String temp = (String) parent.getItemAtPosition(position);
        connectPhoneThread = new ConnectPhoneThread(mBluetoothAdapter.getRemoteDevice(temp.substring(temp.length() - 17)));
        connectPhoneThread.start();
    }


    private class ConnectPhoneThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectPhoneThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

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
            //ConnectedThread connectedThread = new ConnectedThread(mmSocket);
            //connectedThread.start();
            mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
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

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private final String NAME = "Server";

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
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
                    // Do work to manage the connection (in a separate thread)
                    //ConnectedThread connectedThread = new ConnectedThread(socket);
                    //connectedThread.start();
                    connectedThread = new ConnectedThread(socket);
                    connectedThread.start();
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

    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private static final int RECORDER_SAMPLERATE = 8000;
        private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
        private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
        private boolean isRecording = false;
        private int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
        int bufferSize;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //convert short to byte
        private byte[] short2byte(short[] sData) {
            int shortArrsize = sData.length;
            byte[] bytes = new byte[shortArrsize * 2];
            for (int i = 0; i < shortArrsize; i++) {
                bytes[i * 2] = (byte) (sData[i] & 0x00FF);
                bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
                sData[i] = 0;
            }
            return bytes;

        }

        public AudioRecord findAudioRecord() {
            for (int rate : mSampleRates) {
                for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
                    for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
                        try {

                            bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                            if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                                // check if we can instantiate and have a success
                                AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);

                                if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                    return recorder;
                            }
                        } catch (Exception e) {

                        }
                    }
                }
            }
            return null;
        }



        public void run() {
              // buffer store for the stream
            int bytes; // bytes returned from read()
            int BytesPerElement = 2; // 2 bytes in 16bit format


            recorder = findAudioRecord();
            //recorder.release();

            short sData[] = new short[bufferSize];
            recorder.startRecording();
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                bytes = recorder.read(sData, 0, bufferSize);
                // Send the obtained bytes to the UI activity
                mHandler.obtainMessage(SEND_MIC_AUDIO, bytes, -1, bufferSize).sendToTarget();
                //write(short2byte(sData));
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }

    }
}
