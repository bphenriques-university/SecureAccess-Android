package sirs.ist.pt.secureaccess;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Set;

import sirs.ist.pt.secureaccess.ListContent.Content;
import sirs.ist.pt.secureaccess.ListContent.DatabaseHandler;
import sirs.ist.pt.secureaccess.ListContent.Server;


public class ItemListActivity extends Activity
        implements ItemListFragment.Callbacks {

    private static final int REQUEST_ENABLE_BT = 12;
    private boolean mTwoPane = false;

    //derp derp derp
    private String current_mac_address_for_qr_code = null;


    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    public boolean deviceSupportsBluetooth(){
        return btAdapter != null;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive (Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON){
                    populateList();
                }
                else{
                    clearList();
                }
                refreshList();
            }

        }
    };

    private void populateList() {
        //populate list
        for (BluetoothDevice bd : getPairedDevices()){
            Content.addItem(new Content.Item(bd));
        }
    }

    private void clearList() {
        Content.clean();
    }


    public void refreshList(){
        ItemListFragment fragment = (ItemListFragment) getFragmentManager().findFragmentById(R.id.item_list);
        fragment.refreshList();
    }

    public boolean isBluetoothActive(){
        if(!btAdapter.getDefaultAdapter().isEnabled()) {
            return false;
        }else {
            return true;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        if(!deviceSupportsBluetooth()){
            Util.makeToast("Device doesn't support bluetooth... quitting", getApplicationContext());
            return;
        }

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        Content.clean();
        if(!this.isBluetoothActive()){
            Util.makeToast("Turning on Bluetooth...", getApplicationContext());
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else{
            populateList();
        }
        refreshList();

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ItemListFragment) getFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    /**
     * Callback method from {@link ItemListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        String deviceId = id;

        Content.Item mItem = Content.ITEM_MAP.get(id);
        final String mac_addr = mItem.macAddr;

        DatabaseHandler db = new DatabaseHandler(this);
        Server s = db.getSharedKey(mac_addr);
        if(s == null){
            Log.d("##DIALOG##", "Server not configured in this device");

            createConfigureDialog(mac_addr, "");

        }else{
            Log.d("Getting: ", s.getKey());
            Intent intent = new Intent(this, ItemDetailActivity.class);
            intent.putExtra("DEVICE_ID", id);
            startActivity(intent);
        }
    }

    public void createConfigureDialog(final String mac_addr, final String key){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Computer not configured");
        alert.setMessage("Please enter the key: ");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);
        input.setText(key); //null if it doesn't come from qrcode

        final Activity this_activity = this;

        alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.d("CONFIGURE SERVER", "MAC: " + mac_addr + " KEY: " + input.getText().toString());
                DatabaseHandler db = new DatabaseHandler(this_activity);
                db.addServer(new Server(mac_addr, input.getText().toString()));
                Util.makeToast("Configuration complete!", getApplicationContext());
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.d("CONFIGURE SERVER", "CANCELLED");
                Util.makeToast("Configuration cancelled!", getApplicationContext());
            }
        });

        alert.setNeutralButton("QR-Code", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.d("CONFIGURE SERVER", "QR-CODE");

                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                Log.i("QR-CODE ANALYSER", "GETTING CODE FOR MAC: " + mac_addr);
                current_mac_address_for_qr_code = mac_addr;
                startActivityForResult(intent, 0);
            }
        });

        alert.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {

                String key = intent.getStringExtra("SCAN_RESULT");

                Log.i("onQRCodeResult", "MAC_ADDR: " + current_mac_address_for_qr_code);

                createConfigureDialog(current_mac_address_for_qr_code, key);

                Log.i("AppON ACTIVITY RESULT", key);
                // Handle successful scan

            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
                Log.i("App", "Scan unsuccessful");
                createConfigureDialog(current_mac_address_for_qr_code, "");
            }
        }
    }


    public void onItemSelectedLongClick(String id, final String mac_addr){
        Log.d("LONG_CLICK", id + " and mac " + mac_addr);

        DatabaseHandler db = new DatabaseHandler(this);
        Server s = db.getSharedKey(mac_addr);
        if(s == null){
            Util.makeToast("Server not configured yet", getApplicationContext());
        }else{
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle(id);
            alert.setMessage("Do you want to delete?");

            final Activity this_activity = this;
            alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Log.d("LONG_CLICK SERVER", "DELETE");
                    DatabaseHandler db = new DatabaseHandler(this_activity);
                    db.deleteServer(new Server(mac_addr, "dummykey"));
                    Util.makeToast("Deleted configuration", getApplicationContext());
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Log.d("LONG_CLICK SERVER", "DELETE");
                    Util.makeToast("Cancelled", getApplicationContext());
                }
            });

            alert.show();
        }
    }

}
