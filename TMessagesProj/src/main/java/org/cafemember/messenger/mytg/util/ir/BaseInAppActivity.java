package org.cafemember.messenger.mytg.util.ir;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import org.cafemember.messenger.mytg.util.ir.helper.util.InAppError;
import org.cafemember.messenger.mytg.util.ir.helper.util.InAppHelper;


public class BaseInAppActivity extends Activity implements InAppHelper.InAppHelperListener {
    protected InAppHelper inAppHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inAppHelper = new InAppHelper(this, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        inAppHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (inAppHelper != null)
            inAppHelper.onActivityDestroy();
    }

    @Override
    public void onConnectedToIABService() {

    }

    @Override
    public void onCantConnectToIABService(InAppError error) {

        Log.i("InApp","Shahrooz 2 :"+error.getMessage());
    }

    @Override
    public void onConnectionLost() {

    }
}
