package com.praetoriandroid.cameraremote.rpc.basic;

import com.praetoriandroid.cameraremote.rpc.BaseRequest;
import com.praetoriandroid.cameraremote.rpc.BaseResponse;
import com.praetoriandroid.cameraremote.rpc.IllegalResultSizeException;
import com.praetoriandroid.cameraremote.rpc.RpcMethod;
import com.praetoriandroid.cameraremote.rpc.SimpleResponse;
import com.praetoriandroid.cameraremote.rpc.ValidationException;


public class GetGeneralRequest extends BaseRequest<Void, GeneralResponse> {

    public GetGeneralRequest() {
        super(GeneralResponse.class, RpcMethod.getIsoSpeedRate);
    }
}



