package org.cafemember.messenger.mytg.util.ir.helper.interfaces;


import org.cafemember.messenger.mytg.util.ir.helper.util.InAppError;

/**
 * callback listener for login process which indicates the failure or success of the process
 *
 * @author Shima Zeinali
 * @author Khaled Bakhtiari
 * @since 2015-02-14
 */
public interface LoginListener extends BaseInAppListener {

    /**
     * this method is called when the login process ic successfully completed.
     */
    public void onLoginSucceed();

    /**
     * this method is called whenever the process is failed.
     *
     * @param error the occurred error
     */
    public void onLoginFailed(InAppError error);
}
