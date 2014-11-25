package sirs.ist.pt.secureaccess;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import sirs.ist.pt.secureaccess.ListContent.Content;
import sirs.ist.pt.secureaccess.threads.SessionThread;

public class ItemDetailActivity extends Activity {
    private Content.Item mItem;
    private SessionThread currentSession = null;
    private BluetoothDevice device = null;
    TextView logTextView = null;

    boolean firstItemLog = true;

    public void connectionOnClick(View v){
        Button b = (Button) findViewById((R.id.connectionButton));
        b.setClickable(false);
        TextView log = (TextView) findViewById(R.id.connectionTextView);
        log("Trying to establish connection ...");

        try {
            currentSession = new SessionThread(device, log);
            currentSession.start();
        }catch(Exception e){
            this.log("Failed to start connection...");
        }
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

        device = mItem.device;

        logTextView = (TextView) findViewById(R.id.connectionTextView);
        logTextView.setMovementMethod(new ScrollingMovementMethod());

        log("Name: " + device.getName());
        log("Mac address: " + device.getAddress());
    }


    private void log(String msg){
        Log.i("APP:DETAIL", msg);
        if(firstItemLog) {
            logTextView.append(msg);
            firstItemLog = false;
        }else {
            logTextView.append("\n" + msg);
        }
    }
    public void disconnect(){
        log("Disconnecting...");
        if(currentSession != null) {
            currentSession.cancel();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.disconnect();
            navigateUpTo(new Intent(this, ItemListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
