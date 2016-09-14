package com.liyeyu.tasktest.binder;

import android.os.RemoteException;

/**
 * Created by Liyeyu on 2016/9/14.
 */
public class ComputeImpl extends ICompute.Stub {
    @Override
    public int add(int a, int b) throws RemoteException {
        return a+b;
    }
}
