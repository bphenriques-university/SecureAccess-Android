package sirs.ist.pt.secureaccess;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by brunophenriques on 22/11/14.
 */
public class Util {
    public static void makeToast(String msg, Context context){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
