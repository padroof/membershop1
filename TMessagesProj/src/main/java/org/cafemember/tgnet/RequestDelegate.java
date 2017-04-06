package org.cafemember.tgnet;

import android.support.annotation.Keep;

@Keep
public interface RequestDelegate {
    void run(TLObject response, TLRPC.TL_error error);
}
