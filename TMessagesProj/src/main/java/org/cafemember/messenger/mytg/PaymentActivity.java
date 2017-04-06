package org.cafemember.messenger.mytg;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import org.cafemember.messenger.BuildVars;
import org.cafemember.messenger.mytg.listeners.OnJoinSuccess;
import org.cafemember.messenger.mytg.listeners.OnResponseReadyListener;
import org.cafemember.messenger.mytg.util.paytool.IabHelper;
import org.cafemember.messenger.mytg.util.paytool.IabResult;
import org.cafemember.messenger.mytg.util.paytool.Inventory;
import org.cafemember.messenger.mytg.util.paytool.Purchase;
import org.json.JSONObject;

/**
 * Created by Masoud on 3/9/2015.
 */
public class PaymentActivity extends Activity {
    //MyKet
//    private String MARKET_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCITAo+hHxsDv6NVS3up5EomOK/EjR8yPU8KgvjCDVyS/x5h9cs3j0/S6WcUnBT7QGOj6/eGLHjHOuJOpS3N5ebltFyIHUTpvCoVfpdM4qNub/G+W3r2ah9KyXrb16wuFFu5a/8LNaP+wK4UIHv2JSmkCVo25A+9zbYzxCZXxqHTQIDAQAB";

    //Bazar
//    private String MARKET_KEY = "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwCv9ZefYrRfZovotKAf9xxi4jbx2i0NhYUtIix6f8N1FptTTueSheSp4r/qjTsDmqpSJW63taxyVBq9X9SByUyvYGglxp+4X/wfVE+RmA+WEb/DE0JisWXunEir1RvmIntaBlV6GWksIKmA2iJIJ41crreyawFRHujmZpWKwAYf0b4274KtW5j4Hjocv12kCYZDQZZ0D8/+KbiFI0g+UVK4eb0Ny05b424AmXE9mA0CAwEAAQ==";

    //IranApps
//    private String MARKET_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCsWkuT4H5IsY0qvUHVzCsw5/EV9TwN8HPdRK4mnMCb17qqmFwbkYsr3rIJi9FqBMoYCjnVlrBf9v3f2gALDdatei4DbIDhL11yay5LV8eiVOn8zTe1v4uMaJNQM0aVj4VJX41ZYqQJNN5Ls1CODnCNiHNXKXpCI2E9hi9oJGa8oQIDAQAB";

    // Debug tag, for logging
    static final String TAG = "PAYMETN";

    // SKUs for our products: the premium upgrade (non-consumable)
    private String SKU_PREMIUM = "sign_locker";

    // Does the user have the premium upgrade?
    boolean mIsPremium = false;

    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 1234;

//    private Button payBtn;
    // The helper object
    IabHelper mHelper;
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            dismiss();
            Log.i(TAG, "Query inventory finished.");
            if (result.isFailure()) {
                Log.i(TAG, "Failed to query inventory: " + result);

                finish();
                return;
            } else {
                Log.i(TAG, "Query inventory was successful.");
                // does the user have the premium upgrade?
                mIsPremium = inventory.hasPurchase(SKU_PREMIUM);

                Log.i(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
                        if(mIsPremium){
                            grantUser(SKU_PREMIUM,inventory.getPurchase(SKU_PREMIUM));
                            return;
                        }
                showProgress("در حال آماده سازی فرآیند خرید");
                mHelper.launchPurchaseFlow(PaymentActivity.this, SKU_PREMIUM, RC_REQUEST, mPurchaseFinishedListener, "payload-string");

                // update UI accordingly

            }

            Log.i(TAG, "Initial inventory query finished; enabling main UI.");

//            payBtn.setEnabled(true);
        }
    };

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                Log.i(TAG, "Error purchasing: " + result);
                if(result.getResponse() == 7){
                    grantUser(purchase.getSku(),purchase);
                }
                else {

                    Toast.makeText(PaymentActivity.this,"عملیات پرداخت با مشکل مواجه شد",Toast.LENGTH_SHORT).show();
                }
                //paymentState.setText("پرداخت انجام نشد");
                finish();
                return;
            } else if (purchase.getSku().equals(SKU_PREMIUM)) {
                // give user access to premium content and update the UI
                //paymentState.setText("پرداخت با موفقیت انجام شد");
                //paymentState.setTextColor(Color.GREEN);
                Toast.makeText(PaymentActivity.this,"پرداخت با موفقیت انجام شد",Toast.LENGTH_SHORT).show();
                grantUser(purchase.getSku(),purchase);
            }
        }
    };
    private ProgressDialog progressDialog;

    private boolean isPackageInstalled(String pack, Activity activity) {

        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo(pack, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!isPackageInstalled(BuildVars.MARKET_PACKAGE,this)){
            Toast.makeText(this, "شما "+BuildVars.MARKET_NAME_FA+" رو نصب نداری!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
            mHelper = new IabHelper(this, BuildVars.MARKET_KEY);
        try {
            Log.i(TAG, "Starting setup.");
            IabHelper.OnIabSetupFinishedListener lis =null;
            try {
                lis = new IabHelper.OnIabSetupFinishedListener() {
                    public void onIabSetupFinished(IabResult result) {
                        try {
                            Log.i(TAG, "Setup finished.");

                            if (!result.isSuccess()) {
                                // Oh noes, there was a problem.
                                Toast.makeText(PaymentActivity.this,"خطا در اتصال به "+BuildVars.MARKET_NAME_FA,Toast.LENGTH_SHORT).show();
                                Log.i(TAG, "Problem setting up In-app Billing: " + result);
                                dismiss();
                                finish();
                                return;
                            }
                            if(result == null){
                                Log.i(TAG,"Failed");
                            }
                            // Hooray, IAB is fully set up!
                            if(!BuildVars.MARKET_NAME.equals("bazaar")) {
                                showProgress("بررسی خرید های قبل");
                                mHelper.queryInventoryAsync(mGotInventoryListener);
                            }
                            else {

                                showProgress("در حال آماده سازی فرآیند خرید");
                                mHelper.launchPurchaseFlow(PaymentActivity.this, SKU_PREMIUM, RC_REQUEST, mPurchaseFinishedListener, "payload-string");

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
            }catch (Exception e){
                e.printStackTrace();
            }
//            mHelper.enableDebugLogging(true);
            showProgress("در حال اتصال به "+BuildVars.MARKET_NAME_FA);
           mHelper.startSetup(lis);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SKU_PREMIUM = getIntent().getStringExtra("sku");



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
            finish();
        } else {
            Log.i(TAG, "onActivityResult handled by IABUtil.");
        }
    }


    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
            if (mHelper != null) mHelper.dispose();
            mHelper = null;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void grantUser(final String sku, final Purchase purchase) {
        showProgress("بررسی صحت خرید");
        mHelper.consumeAsync(purchase
                , new IabHelper.OnConsumeFinishedListener() {
                    @Override
                    public void onConsumeFinished(Purchase purchase, IabResult result) {
                        if(result.isSuccess()){

                            Commands.checkBoughtItem(sku, purchase.getOriginalJson(), new OnResponseReadyListener() {
                                @Override
                                public void OnResponseReady(boolean error, JSONObject data, String message
                                ) {
                                    dismiss();
                                    if (!error) {

                                        Toast.makeText(PaymentActivity.this,"خرید انجام شد",Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        {

                                            if(message == null || message.length() == 0){
                                                message = "خطا در تایید صحت خرید";
                                            }
                                            Toast.makeText(PaymentActivity.this,message,Toast.LENGTH_SHORT).show();
                                            finish();}
                                    }

                                    finish();
                                }
                            });                           }
                        else {
                            dismiss();
                            Toast.makeText(PaymentActivity.this,"سکه خریداری شده مصرف نشد!",Toast.LENGTH_SHORT).show();
                            finish();
                        }

                    }
                });

    }

    private void showProgress(String message){

        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void dismiss(){

        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }
}