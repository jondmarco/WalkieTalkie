package jdm.walkietalkie;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import java.util.Set;
import java.util.UUID;
import jdm.walkietalkie.threads.ConnectPhoneThread;
import jdm.walkietalkie.threads.ConnectedThread;
import jdm.walkietalkie.threads.AcceptThread;
import jdm.walkietalkie.threads.RecordingThread;

public class MainActivity extends ActionBarActivity implements ListView.OnClickListener, ListView.OnItemClickListener, View.OnLongClickListener {

    private ListView lvPaired, lvDiscovered;
    private Button bScan, bTalk;
    private ArrayAdapter<String> pAdapter, dAdapter;
    private BluetoothAdapter mBluetoothAdapter;

    private ConnectPhoneThread connectPhoneThread;
    private AcceptThread acceptThread;
    private ConnectedThread connectedThread;

    private final int SUCCESS_CONNECT = 0;
    private final int RECEIVE_AUDIO = 1;
    private final UUID APP_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    private  RecordingThread recordingThread;

    int track;

    private static Handler mHandler = null;

    protected Context activityContext;
    private ConnectedThread connectingThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activityContext = this;
        enableBluetooth();
        setupHandler();
        setupViews();
        showPairedDevices();
    }

    private void enableBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Does not support Bluetooth",Toast.LENGTH_LONG).show();
        }
        acceptThread = new AcceptThread(mBluetoothAdapter, APP_UUID);
        acceptThread.start();
        track = 0;

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    private void setupHandler() {

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case SUCCESS_CONNECT:
                        connectedThread = new ConnectedThread((BluetoothSocket) msg.obj);
                        connectedThread.start();
                        break;
                    case RECEIVE_AUDIO:
                        // and play on speaker
                        byte[] readbuf = (byte[]) msg.obj;
                        String s = new String(readbuf);
                        if (s.length() > 0) {
                            Toast.makeText(activityContext, s, Toast.LENGTH_LONG).show();
                        }
                        break;
                }
            }
        };
    }

    protected void setupViews() {
        lvPaired = (ListView) findViewById(R.id.lvPaired);
        lvDiscovered = (ListView) findViewById(R.id.lvDiscovered);
        lvDiscovered.setOnItemClickListener(this);
        bScan = (Button) findViewById(R.id.bScan);
        bScan.setOnClickListener(this);
        bTalk = (Button) findViewById(R.id.bTalk);
        bTalk.setOnClickListener(this);
        bTalk.setOnLongClickListener(this);
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
            case R.id.bTalk:

                break;
            default:

                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String temp = (String) parent.getItemAtPosition(position);
        connectPhoneThread = new ConnectPhoneThread(mBluetoothAdapter.getRemoteDevice(temp.substring(temp.length() - 17)), mBluetoothAdapter, APP_UUID);
        connectPhoneThread.start();
    }

    @Override
    public boolean onLongClick(View v) {
            connectedThread.getRecordingThread();


        return false;
    }

    public static Handler getHandler() {
        return mHandler;
    }
}

