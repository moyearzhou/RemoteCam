package com.praetoriandroid.cameraremote.rpc;

import android.util.Log;

public class SetSelfTimerRequest extends BaseRequest<Integer, SimpleResponse> {

    public static final int NO_TIMER = 0;

    public SetSelfTimerRequest(int timer) {
        super(SimpleResponse.class, RpcMethod.setSelfTimer, timer);
    }
}
