package com.praetoriandroid.cameraremote.rpc.basic;

import com.praetoriandroid.cameraremote.rpc.BaseResponse;
import com.praetoriandroid.cameraremote.rpc.IllegalResultSizeException;
import com.praetoriandroid.cameraremote.rpc.ValidationException;

public class GeneralResponse extends BaseResponse<Integer> {

    @Override
    public void validate() throws ValidationException {
        super.validate();

        if (isOk()) {
            Integer[] result = getResult();
            if (result.length != 1) {
                throw new IllegalResultSizeException(1, result.length);
            }
        }
    }

    public int getValue() {
        return getResult()[0];
    }
}