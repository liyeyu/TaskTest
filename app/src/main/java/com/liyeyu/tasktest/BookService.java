package com.liyeyu.tasktest;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BookService extends Service {
    //支持并发读写，
    private static CopyOnWriteArrayList mList = new CopyOnWriteArrayList();
    //系统提供的用于跨进程管理回调接口的集合
    private static RemoteCallbackList<INewBookListener> mListeners = new RemoteCallbackList<>();
    private AtomicBoolean mServiceAlive = new AtomicBoolean(true);
    private AtomicInteger bookId = new AtomicInteger(10);
    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mServiceAlive.get() && mList.size()<10){
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Book book = new Book(bookId.getAndAdd(1));
                    mList.add(book);
                    notifyAllListener(book);
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mServiceAlive.set(false);
    }

    private void notifyAllListener(Book book){
        //开始广播回调
        int N = mListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            INewBookListener bookListener = mListeners.getBroadcastItem(i);
            if(bookListener!=null){
                try {
                    bookListener.onAddNewBook(book);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        mListeners.finishBroadcast();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //权限校验，也可在Stub的onTransact方法中检验
        int check = checkCallingOrSelfPermission("com.liyeyu.tasktest.permission.ACCESS_BOOK_SERVICE");
        Log.i("permission","check:"+check);
        if(check != PackageManager.PERMISSION_GRANTED){
            return null;
        }
        return BookManager.verify(this);
    }

    public static class BookManager extends IBookAidlInterface.Stub{

        private BookManager() {
        }
        //这里通过获取客户端的包名进行校验
        public static BookManager verify(Context context){
            String[] packs = context.getPackageManager().getPackagesForUid(getCallingUid());
            String pack = null;
            if(packs!=null && packs.length>0){
                pack = packs[0];
                Log.i("verify","pack:"+pack);
            }
            if(!TextUtils.isEmpty(pack) && pack.startsWith("com.liyeyu")){
                return new BookManager();
            }
            return null;
        }

        @Override
        public void add(Book book) throws RemoteException {
            mList.add(book);
            Log.i("BookManager","book:"+book.bookId);
//            Log.i("process","service:"+Thread.currentThread().getName());
        }

        @Override
        public List<Book> getBookList() throws RemoteException {
            //服务端返回的是系统处理过后的ArrayList
            return mList;
        }

        @Override
        public void registerListener(INewBookListener listener) throws RemoteException {
            mListeners.register(listener);
            Log.i("book listener","register:"+listener.getClass().getName());
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void unregisterListener(INewBookListener listener) throws RemoteException {
            mListeners.unregister(listener);
            Log.i("book listener","unregister "+listener.getClass().getName());
        }
    }
}
