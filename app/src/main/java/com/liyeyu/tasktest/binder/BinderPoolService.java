package com.liyeyu.tasktest.binder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BinderPoolService extends Service {

    BinderPool.BinderPoolImpl mBinderPool = new BinderPool.BinderPoolImpl();

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("BinderPoolService","onBind");
        return mBinderPool;
    }

}
