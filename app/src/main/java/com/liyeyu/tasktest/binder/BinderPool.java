package com.liyeyu.tasktest.binder;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Liyeyu on 2016/9/14.
 */
public class BinderPool {

    public static final int BINDER_NONE = -1;
    public static final int BINDER_COMPUTE = 0;
    public static final int BINDER_SECURITY = 1;

    Context mContext;
    private static volatile BinderPool mBinderPool;
    private CountDownLatch mDownLatch;
    private IBinderPool mIBinderPool;

    private BinderPool(Context context) {
        mContext = context.getApplicationContext();
        connectBinderPoolService();
    }

    public static BinderPool getInstance(Context context){
        if(mBinderPool==null){
            synchronized (BinderPool.class){
                if(mBinderPool==null){
                    mBinderPool = new BinderPool(context);
                }
            }
        }
        return mBinderPool;
    }

    private synchronized void connectBinderPoolService(){
        //线程等待，计数完成后才能继续，同步
        mDownLatch = new CountDownLatch(1);
        Intent intent = new Intent(mContext,BinderPoolService.class);
        mContext.bindService(intent,mServiceConnection, Service.BIND_AUTO_CREATE);
        try {
            mDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public IBinder queryBinder(int binderCode){
        IBinder iBinder = null;
        if(mIBinderPool!=null){
            try {
                iBinder = mIBinderPool.queryBinder(binderCode);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return iBinder;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mIBinderPool = IBinderPool.Stub.asInterface(iBinder);
            try {
                mIBinderPool.asBinder().linkToDeath(mDeathRecipient,IBinder.FLAG_ONEWAY);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            //绑定服务成功，计数
            mDownLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            mIBinderPool.asBinder().unlinkToDeath(mDeathRecipient,IBinder.FLAG_ONEWAY);
            mIBinderPool = null;
            connectBinderPoolService();
        }
    };

    public static class BinderPoolImpl extends IBinderPool.Stub{

        @Override
        public IBinder queryBinder(int binderCode) throws RemoteException {

            IBinder iBinder = null;
            switch (binderCode){
                case BINDER_COMPUTE:
                    iBinder = new ComputeImpl();
                    break;
                case BINDER_SECURITY:
                    iBinder = new SecurityCenterImpl();
                    break;
            }
            return iBinder;
        }
    }
}
