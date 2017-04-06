package org.cafemember.messenger.mytg;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.cafemember.messenger.BuildVars;
import org.cafemember.messenger.mytg.listeners.OnJoinSuccess;
import org.cafemember.messenger.mytg.listeners.OnResponseReadyListener;
import org.cafemember.messenger.mytg.util.ir.BaseInAppActivity;
import org.cafemember.messenger.mytg.util.ir.helper.interfaces.BuyProductListener;
import org.cafemember.messenger.mytg.util.ir.helper.interfaces.ConsumeListener;
import org.cafemember.messenger.mytg.util.ir.helper.interfaces.LoginListener;
import org.cafemember.messenger.mytg.util.ir.helper.interfaces.PurchasesListener;
import org.cafemember.messenger.mytg.util.ir.helper.model.PurchaseData;
import org.cafemember.messenger.mytg.util.ir.helper.model.PurchaseItem;
import org.cafemember.messenger.mytg.util.ir.helper.util.InAppError;
import org.cafemember.messenger.mytg.util.ir.helper.util.InAppKeys;
import org.cafemember.messenger.mytg.util.ir.util.Util;
import org.cafemember.messenger.mytg.util.paytool.IabHelper;
import org.cafemember.messenger.mytg.util.paytool.IabResult;
import org.cafemember.messenger.mytg.util.paytool.Inventory;
import org.cafemember.messenger.mytg.util.paytool.Purchase;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Masoud on 3/9/2015.
 */
public class IranPaymentActivity extends BaseInAppActivity {
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
            Log.i(TAG, "Starting setup.");
        SKU_PREMIUM = getIntent().getStringExtra("sku");
            start();



    }

    private void purchase(){
        final AlertDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("در حال دریافت خرید های قبلی");
        progressDialog.show();

        inAppHelper.getPurchases(new PurchasesListener() {

            @Override
            public void onGotPurchases(final ArrayList<PurchaseData> purchases, String continuationToken) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        if (purchases.size() != 0) {
                            for(PurchaseData p: purchases){
                                if(p.sku.equals(SKU_PREMIUM)){
                                    try {
                                        grantUser(SKU_PREMIUM, p.purchaseItem.jsonObject.toString(0));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                }
                            }
                            Util.showAlertDialog(IranPaymentActivity.this, "خرید های قبل معتبر نیستند", true);
                        } else {
                            Util.showAlertDialog(IranPaymentActivity.this, "شما خریدی از قبل ندارید", true);
                        }
                    }
                });
            }

            @Override
            public void onFailedGettingPurchases(final InAppError errorCode) {

                Log.i("InApp","Shahrooz :"+errorCode.getMessage());
                progressDialog.dismiss();
                if (errorCode.getErrorCode() == InAppKeys.BILLING_RESPONSE_USER_NOT_LOGIN) {
                    inAppHelper.loginUser(new LoginListener() {
                        @Override
                        public void onLoginSucceed() {
                            //after than logging should getPurchase process done again
                            start();
                        }

                        @Override
                        public void onLoginFailed(InAppError errorCode) {
                            Util.showAlertDialogBackground(IranPaymentActivity.this, "خطا در ورود به ایران اپس", true);
                        }
                    });
                } else {
                    Util.showAlertDialogBackground(IranPaymentActivity.this, errorCode.getMessage(), true);
                }
            }
        });
    }
    private void start(){
        try {
            inAppHelper.buyProduct(SKU_PREMIUM, null, true, new BuyProductListener() {

                @Override
                public void onBuyProductSucceed(PurchaseItem purchaseItem) {
                    try {
                        grantUser(purchaseItem.productId,purchaseItem.jsonObject.toString(0));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Util.showAlertDialog(IranPaymentActivity.this, purchaseItem.productId + " was purchased", true);
                }

                @Override
                public void onBuyProductFailed(InAppError error) {
                    if (error == InAppError.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED){
                        purchase();

                    }
                    if (error.getErrorCode() == InAppKeys.BILLING_RESPONSE_USER_NOT_LOGIN) {
                        inAppHelper.loginUser(new LoginListener() {
                            @Override
                            public void onLoginSucceed() {
                                //after than logging should getPurchase process done again
                                start();
                            }

                            @Override
                            public void onLoginFailed(InAppError errorCode) {
                                Util.showAlertDialogBackground(IranPaymentActivity.this, "خطا در ورود به ایران اپس", true);
                            }
                        });
                    } else {
                        Util.showAlertDialogBackground(IranPaymentActivity.this, error.getMessage(), true);
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void grantUser(final String sku, final String purchase) {

        Commands.checkBoughtItem(sku, purchase, new OnResponseReadyListener() {
            @Override
            public void OnResponseReady(boolean error, JSONObject data, String message
            ) {
                if (!error) {
                    inAppHelper.consumeProduct(sku, new ConsumeListener() {
                        @Override
                        public void onConsumeSucceed() {


                            Toast.makeText(IranPaymentActivity.this,"خرید انجام شد",Toast.LENGTH_SHORT).show();

                            finish();
                        }

                        @Override
                        public void onItemNotOwned() {
                            Toast.makeText(IranPaymentActivity.this,"خرید انجام شد ولی "+BuildVars.MARKET_NAME_FA+" نفهمید!",Toast.LENGTH_SHORT).show();

                            finish();
                        }

                        @Override
                        public void onConsumeFailed(InAppError error) {
                            Toast.makeText(IranPaymentActivity.this,"خرید انجام شد ولی "+" مصرف نشد!"+error.getMessage(),Toast.LENGTH_SHORT).show();

                            finish();
                        }
                    });

                }
                else {
                    {
                        if(message == null || message.length() == 0){
                            message = "خطا در تایید صحت خرید";
                        }
                        Toast.makeText(IranPaymentActivity.this,message,Toast.LENGTH_SHORT).show();
                        finish();}
                }

            }
        });

    }
}