package org.cafemember.messenger.mytg.util.ir.helper.interfaces;

import java.util.ArrayList;

import org.cafemember.messenger.mytg.util.ir.helper.model.Product;
import org.cafemember.messenger.mytg.util.ir.helper.util.InAppError;


/**
 * callback listener for get sku process which indicates the failure or success of the process
 *
 * @author Shima Zeinali
 * @author Khaled Bakhtiari
 * @since 2015-02-14
 */
public interface SkuDetailListener extends BaseInAppListener {

    /**
     * this method is called when the get sku process ic successfully completed.
     *
     * @param productDetails detail of the sku
     */
    public void onGotSkus(ArrayList<Product> productDetails);

    /**
     * this method is called whenever the process is failed.
     *
     * @param error the occurred error
     */
    public void onFailedGettingSkus(InAppError error);

}
