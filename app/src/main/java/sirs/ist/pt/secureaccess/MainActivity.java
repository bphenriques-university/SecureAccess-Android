package sirs.ist.pt.secureaccess;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends Activity {
    private BluetoothManager bluetoothManager;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothManager.destroy();
    }

    public void sendMessage(View view){
        bluetoothManager.sendMessage();
    }

    public void connectToDevice(View view) {
        Log.i("CONN", "Trying to establish connection to device");
        bluetoothManager.connect(bluetoothManager.getSelectedDevice());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fillTablePairedDevices(){

        final ListView listview = (ListView) findViewById(R.id.listview);

        //getting paired devices and putting them on a list
        final ArrayList<BluetoothDevice> pairedDevices = bluetoothManager.getPairedDevices();
        final ArrayList<String> list = new ArrayList<String>();

        for(BluetoothDevice d : pairedDevices){
            list.add(d.getName() + "\n" + d.getAddress());
        }

        final StableArrayAdapter adapter = new StableArrayAdapter(this, android.R.layout.simple_list_item_1, list);

        listview.setAdapter(adapter);

        final Button b = (Button)findViewById(R.id.connectButton);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);

                for(int i = 0; i < parent.getCount(); i++){
                    parent.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                }

                parent.getChildAt(position).setBackgroundColor(Color.rgb(201,201,201));
                b.setText("Connect to\n" + pairedDevices.get(position).getName());
                b.setVisibility(View.VISIBLE);
                bluetoothManager.setSelectedDevice(pairedDevices.get(position));
            }
        });
    }


    public void checkBluetoothStatus() {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter == null) {
            Util.makeToast("Bluetooth NOT supported. Aborting.", getApplicationContext());
            return;
        }
        else if(!btAdapter.getDefaultAdapter().isEnabled()) {
            Util.makeToast("Bluetooth Off. Turn on and restart App", getApplicationContext());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.list_devices);

        this.checkBluetoothStatus();

        final Button b = (Button)findViewById(R.id.connectButton);
        b.setVisibility(View.INVISIBLE);


        bluetoothManager = new BluetoothManager();
        fillTablePairedDevices();
    }

    /* ******** Only useful to tables ************ */

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

}
