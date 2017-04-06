package org.cafemember.messenger.mytg;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import org.cafemember.messenger.mytg.listeners.OnJoinSuccess;
import org.cafemember.messenger.mytg.listeners.OnResponseReadyListener;
import org.cafemember.messenger.mytg.util.paytool.IabHelper;
import org.cafemember.messenger.mytg.util.paytool.IabResult;
import org.cafemember.messenger.mytg.util.paytool.Purchase;
import org.json.JSONObject;

import io.nivad.iab.BillingProcessor;

import io.nivad.iab.MarketName;
import io.nivad.iab.TransactionDetails;

public class PayActivityNivad extends Activity {


	static final int RC_REQUEST = 10001;

	private static final String NIVAD_APP_ID = "03505597-6d09-4e98-a8c0-e681ec0a8970";
    private static final String NIVAD_APP_SECRET = "TTkC2agsPvDWqhy1L5qbLLp42AdbptuChw0CgsXYqhSFXLxAlUXQGZYxpZcuzdRf";
    final String BAZAAR_KEY = "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwDyr2kpxfV+k4xFY01DK9MTP7xrhzd9SSv9TqrXGAiU2v99mlYsTu5MeBC/4zqLlPd+MDwc5qkCE6/agjmCaY2KCZjKIvXox3GtpZ8rjalyUZa3Srzz7frSiv3TlnIhiT0+fDPuHc2ldpVrbZ95W1mYErDbftWa0Sx/k1ZOc+2qLO6FsCceJs3GCKyuv5upTL2RpSARh9n/LDdxwjkTeJhRTsNBYt2T8cXwtCpcoIsCAwEAAQ==";
    final String MYKET_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDEJNtjT0jd1UH3gH9nhcGQJFz/NBwjTg4iXeq24TPltOuecCa+0gY0iEw2eyRNN7n3xqw9jPRLguA1l8rqMQ5JLqPHJ8tFog+XhMGrTWScx5u+XTFO8kF7ZAVyfp+xblRyOuXRmdkzeMq5TnDewNgFQfxK0EhmQys30GWO0DZdWwIDAQAB";

	IabHelper mHelper;

	ListView listView;
	Dialog progressDialog;
	private String sku;
	private BillingProcessor mNivadBilling;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sku = getIntent().getStringExtra("sku");
	//	mNivadBilling = new BillingProcessor(this, BAZAAR_KEY, NIVAD_APP_ID, NIVAD_APP_SECRET, mBillingMethods);
  //mNivadBilling = new BillingProcessor(this,MYKET_KEY ,NIVAD_APP_ID, NIVAD_APP_SECRET, MarketName.MYKET, mBillingMethods);
    mNivadBilling = new BillingProcessor(this,BAZAAR_KEY ,NIVAD_APP_ID, NIVAD_APP_SECRET, MarketName.CAFE_BAZAAR, mBillingMethods);


	}


	private BillingProcessor.IBillingHandler mBillingMethods = new BillingProcessor.IBillingHandler() {

		@Override
		public void onBillingInitialized() {
			// این متد زمانی که سرویس پرداخت درون برنامه‌ای آماده‌ی کار می‌شود فراخوانی می‌شود
            mNivadBilling.purchase(PayActivityNivad.this, sku);

        }


		@Override
		public void onProductPurchased(String sku, final TransactionDetails details) {
			// این متد پس از خرید موفق فراخوانی می‌شود
            Commands.checkBoughtItem(sku,details.purchaseInfo.responseData, new OnResponseReadyListener() {
                @Override
                public void OnResponseReady(boolean error, JSONObject data, String message
                ) {
                    if (!error) {
                        if (mNivadBilling.consumePurchase(details.productId)) {
                            Toast.makeText(PayActivityNivad.this,"خرید انجام شد",Toast.LENGTH_SHORT).show();


                        } else {
                          Toast.makeText(PayActivityNivad.this,"خرید انجام شد",Toast.LENGTH_SHORT).show();
                        }
                    }

                    finish();
                }
            });
		}

		@Override
		public void onBillingError(int code, Throwable error) {
			// این متد زمانی که اشکالی در فرایند پرداخت به وجود بیاید فراخوانی می‌شود

           // Toast.makeText(PayActivityNivad.this,"خطا "+code,Toast.LENGTH_SHORT).show();
        finish();
        }


		@Override
		public void onPurchaseHistoryRestored() {
			// این متد زمانی فراخوانی می‌شود که لیست محصولاتی که کاربر خریده اما هنوز مصرف نشده‌اند از بازار دریافت شده اند
		}
	};

	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (!mNivadBilling.handleActivityResult(requestCode, resultCode, data)){
            super.onActivityResult(requestCode, resultCode, data);
            finish();
        }
    }
    
    @Override
    protected void onDestroy() {
        if (mNivadBilling != null)
            mNivadBilling.release();
    	super.onDestroy();

    }
	
    

    
    
     
}
