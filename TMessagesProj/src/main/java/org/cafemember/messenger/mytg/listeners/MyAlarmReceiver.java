package org.cafemember.messenger.mytg.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.cafemember.messenger.mytg.util.Defaults;

public class MyAlarmReceiver extends BroadcastReceiver {
    public MyAlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("RCV","Triggered");
        Defaults.getInstance().setFetchAccess(true);
        Toast.makeText(context,"کانال های ممبر شاپ آماده اند",Toast.LENGTH_SHORT).show();
    }
}
