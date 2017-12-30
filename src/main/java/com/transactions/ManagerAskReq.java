package com.transactions;

import com.Request;
import rmi.Context;

public class ManagerAskReq extends Request {
    public ManagerAskReq() {
    }

    public ManagerAskReq(Context context) {
        super(context);
    }
}
