package org.cafemember.messenger.mytg.util.ir.helper.interfaces;


import org.cafemember.messenger.mytg.util.ir.helper.model.PurchaseItem;
import org.cafemember.messenger.mytg.util.ir.helper.util.InAppError;

/**
 * callback listener for buying process which indicates the failure or success of the process
 *
 * @author Shima Zeinali
 * @author Khaled Bakhtiari
 * @since 2015-02-14
 */
public interface BuyProductListener extends BaseInAppListener {

    /**
     * this method is called when the buying process ic successfully completed.
     *
     * @param purchaseItem data of the bought product
     */
    public void onBuyProductSucceed(PurchaseItem purchaseItem);

    /**
     * this method is called whenever the process is failed.
     *
     * @param error the occurred error
     */
    public void onBuyProductFailed(InAppError error);
}
