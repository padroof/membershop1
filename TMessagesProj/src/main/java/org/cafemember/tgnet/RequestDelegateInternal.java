package org.cafemember.tgnet;

import android.support.annotation.Keep;

@Keep
public interface RequestDelegateInternal {
    void run(int response, int errorCode, String errorText);
}
