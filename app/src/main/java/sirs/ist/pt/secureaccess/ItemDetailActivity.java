package sirs.ist.pt.secureaccess;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import sirs.ist.pt.secureaccess.ListContent.Content;
import sirs.ist.pt.secureaccess.threads.SessionThread;

public class ItemDetailActivity extends Activity{
    private Content.Item mItem;
    private SessionThread currentSession = null;
    private BluetoothDevice device = null;
    TextView logTextView = null;

    boolean firstItemLog = true;

    public boolean waitingForConnect = true;

    public void connect(){
        TextView log = (TextView) findViewById(R.id.connectionTextView);

        try {
            currentSession = new SessionThread(device, this);
            currentSession.start();
        }catch(Exception e){
            this.log("APP:DETAIL", "Failed to start connection...");
        }
    }

    public void log(final String id, final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(id, msg);
                if(firstItemLog) {
                    logTextView.append(msg);
                    firstItemLog = false;
                }else {
                    logTextView.append("\n" + msg);
                }
            }
        });
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

        ScrollView mScrollView = (ScrollView) findViewById(R.id.SCROLLER_ID);
        mScrollView.fullScroll(View.FOCUS_DOWN);

        logTextView.setMovementMethod(new ScrollingMovementMethod());

        log("APP:DETAIL","Name: " + device.getName());
        log("APP:DETAIL","Mac address: " + device.getAddress());
        log("APP:DETAIL","        Tap screen to connect");

        View myView = findViewById(R.id.item_detail_container);
        myView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if(waitingForConnect) {
                    connect();
                }
                waitingForConnect = false;
                return true;
            }
        });

    }

    public void disconnect(){
        log("APP:DETAIL","Disconnecting...");
        if(currentSession != null) {
            currentSession.interrupt();
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
