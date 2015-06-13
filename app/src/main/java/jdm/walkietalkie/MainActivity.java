package jdm.walkietalkie;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;


public class MainActivity extends ActionBarActivity implements ListView.OnClickListener {

    ListView lvPaired, lvDiscovered;
    Button bScan, bDiscover;
    ArrayAdapter<String> pAdapter, dAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();
        showPairedDevices();
    }

    protected void setupViews() {
        lvPaired = (ListView) findViewById(R.id.lvPaired);
        lvDiscovered = (ListView) findViewById(R.id.lvDiscovered);
        bScan = (Button) findViewById(R.id.bScan);
        bDiscover = (Button) findViewById(R.id.bDiscover);
    }

    protected void showPairedDevices() {
        pAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,getPairedDevices());
        lvPaired.setAdapter(pAdapter);
    }
    protected void showDiscoveredDevices() {
        dAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,discoverDevices());
        lvDiscovered.setAdapter(dAdapter);
    }

    protected String[] getPairedDevices() {
        return new String[] {"3"};
    }

    protected String[] discoverDevices() {
        return new String[] {"3"};
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
                break;
            case R.id.bDiscover:

                break;
            default:

                break;
        }
    }
}
