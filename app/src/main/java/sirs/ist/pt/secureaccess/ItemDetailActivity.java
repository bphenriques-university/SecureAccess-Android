package sirs.ist.pt.secureaccess;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import sirs.ist.pt.secureaccess.ListContent.Content;


/**
 * An activity representing a single Item detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ItemListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link ItemDetailFragment}.
 */
public class ItemDetailActivity extends Activity {
    private Content.Item mItem;


    public void connectionOnClick(View v){
        Button b = (Button) findViewById((R.id.connectionButton));
        b.setClickable(false);
        TextView log = (TextView) findViewById(R.id.connectionTextView);
        log.append("\nTrying to connect...");
        Log.i("CONN", "Trying to establish connection to device");
        BluetoothManager.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);


        Intent intent = getIntent();
        String id = intent.getStringExtra("DEVICE_ID");

        mItem = Content.ITEM_MAP.get(id);

        this.setTitle(mItem.name);

        BluetoothDevice device = mItem.device;
        BluetoothManager.setSelectedDevice(device);
        TextView log = (TextView) findViewById(R.id.connectionTextView);
        log.append("Name: " + device.getName());
        log.append("\nMAC: " + device.getAddress());
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            BluetoothManager.disconnect();
            navigateUpTo(new Intent(this, ItemListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
