package org.cafemember.messenger.mytg.util.ir.helper.interfaces;

import java.util.ArrayList;

import org.cafemember.messenger.mytg.util.ir.helper.model.PurchaseData;
import org.cafemember.messenger.mytg.util.ir.helper.util.InAppError;


/**
 * callback listener for purchase process which indicates the failure or success of the process
 *
 * @author Shima Zeinali
 * @author Khaled Bakhtiari
 * @since 2015-02-14
 */
public interface PurchasesListener extends BaseInAppListener {

    /**
     * this method is called when the purchase process ic successfully completed.
     *
     * @param purchase          data of the purchase product
     * @param continuationToken In case the number of purchased items is more than 100,
     *                          this key will hold a value that you could use to receive the rest of purchase data
     */
    public void onGotPurchases(ArrayList<PurchaseData> purchase, String continuationToken);

    /**
     * this method is called whenever the process is failed.
     *
     * @param error the occurred error
     */
    public void onFailedGettingPurchases(InAppError error);

}
