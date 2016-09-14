package com.liyeyu.tasktest;

import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.liyeyu.tasktest.binder.BinderPool;
import com.liyeyu.tasktest.binder.ISecurityCenter;
import com.liyeyu.tasktest.binder.SecurityCenterImpl;
import com.liyeyu.tasktest.socket.TCPServerService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * IPC机制，序列化
 */
public class MainActivity extends AppCompatActivity {


    private IBookAidlInterface mIBookAidl;
    private IBinder.DeathRecipient mDeathRecipient;
    private ServiceConnection mRecipientConnection;
    private ServiceConnection mMessengerConnection;
    private Messenger mMessenger;
    private ContentObserver mContentObserver;
    private ContentResolver mContentResolver;
    private PrintWriter mPrintWriter;
    private BufferedReader mBufferedReader;
    private Socket mSocket;
    private IBinder mIBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //对象是不能直接进行跨进程传输的，本质上都是序列化和反序列化的结果
        //序列化反序列化的结果只是内容相同，而并非同一对象

//        parcelableEntity();
//        bindMessenger();
//        proxyBinder();
//        testBookProvider();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                startSocket();
//            }
//        }).start();
       new Thread(new Runnable() {
            @Override
            public void run() {
                binderPool();
            }
        }).start();
    }

    private void binderPool() {
        IBinder iBinder =  BinderPool.getInstance(this).queryBinder(BinderPool.BINDER_SECURITY);
        ISecurityCenter mSecurityBinder = SecurityCenterImpl.asInterface(iBinder);
        String text = "安卓";
        try {
            String encrypt = mSecurityBinder.encrypt(text);
            String decrypt = mSecurityBinder.decrypt(encrypt);
            Log.i("ISecurityCenter","encrypt:"+encrypt+" decrypt:"+decrypt);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void parcelableEntity(){
        Book book = new Book(1);

        Book1 book1 = new Book1(21);
        String path = Environment.getExternalStorageDirectory()+"/book1.txt";
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path));
            outputStream.writeObject(book1);
            outputStream.flush();
            outputStream.close();

            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(path));
            Book1 book2 = (Book1) inputStream.readObject();
            inputStream.close();
//            Log.e("book2:",""+book2.bookId);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * IPC-socket
     */
    private void startSocket() {
        startService(new Intent(MainActivity.this, TCPServerService.class));
        while(mSocket ==null){
            try {
                mSocket = new Socket("localhost",8688);
                mPrintWriter = new PrintWriter(
                        new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())),true);
                Log.i("Socket","connect");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            while(!isFinishing()){
                String line = mBufferedReader.readLine();
                if(line!=null){
                    mSocketHandler.obtainMessage(0,line).sendToTarget();
                }
            }
            mBufferedReader.close();
            mSocket.shutdownInput();
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * IPC-provider
     */
    private void testBookProvider() {
        Uri bookUri = Uri.parse(BookProvider.BOOK_CONTENT_URI);
        Uri userUri = Uri.parse(BookProvider.USER_CONTENT_URI);
        mContentResolver = getContentResolver();
        mContentObserver = new ContentObserver(mProviderHandler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                Log.i("mProviderHandler",uri.getPath());
            }
        };
        mContentResolver.registerContentObserver(bookUri, true, mContentObserver);
        mContentResolver.registerContentObserver(userUri, true, mContentObserver);
        ContentValues contentValues = new ContentValues();
        contentValues.put("name","Android1");
        contentValues.put("_id","3");
        mContentResolver.insert(bookUri,contentValues);

        mContentResolver.query(userUri, null, null, null, null);
        Cursor bookCursor = mContentResolver.query(bookUri, null, null, null, null);

        if(bookCursor!=null){
            while (bookCursor.moveToNext()){
                int id = bookCursor.getInt(0);
                String name = bookCursor.getString(1);
                Book book = new Book(id,name);
                Log.i("query",book.bookId+","+book.name);
            }
        }
        bookCursor.close();
    }

    /**
     * 通过Messenger串行机制处理IPC
     */
    private void bindMessenger() {
        //通过信使Messenger实现服务端和客户端互通
        mMessengerConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                //这里获取到服务端的IBinder Messenger对象，用于向服务端发送信息，由服务端的handler处理信息
                mMessenger = new Messenger(iBinder);
                Message msg = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString("msg","client send msg");
                //这里绑定客户端的Messenger对象到服务端，用于接收服务端发送的信息，由客户端的handler处理信息
                msg.replyTo = mGetMessenger;
                msg.setData(bundle);
                try {
                    mMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        bindService(new Intent(this,MessengerService.class), mMessengerConnection, Service.BIND_AUTO_CREATE);
    }

    /**
     * IPC-Binder
     */
    private void proxyBinder() {
//        Log.e("process"," Main:"+Thread.currentThread().getName());
        mRecipientConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mIBookAidl = IBookAidlInterface.Stub.asInterface(iBinder);
                try {
                    mIBookAidl.add(new Book(333));
                    mIBookAidl.asBinder().linkToDeath(mDeathRecipient,IBinder.FLAG_ONEWAY);
                    mIBookAidl.registerListener(mINewBookListener);
                    Log.i("book list",mIBookAidl.getBookList().size()+"");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                //在UI线程中回调
            }
        };
        bindService(new Intent(this,BookService.class), mRecipientConnection, Service.BIND_AUTO_CREATE);
        //Binder断裂代理
        mDeathRecipient = new IBinder.DeathRecipient() {
            @Override
            public void binderDied() {
                //在binder线程池中回调，不能访问UI
                if(mIBookAidl!=null && !mIBookAidl.asBinder().isBinderAlive()){
                    mIBookAidl.asBinder().unlinkToDeath(this,IBinder.FLAG_ONEWAY);
                    mIBookAidl = null;
                }
            }
        };
    }

    private INewBookListener mINewBookListener = new INewBookListener.Stub() {
        @Override
        public void onAddNewBook(final Book book) throws RemoteException {
            //切换到UI线程
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("book notify","id:"+book.bookId);
                    try {
                        Log.i("book list",mIBookAidl.getBookList().size()+"");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    };

    private static final Handler mGetHandler  = new Handler(){
        @Override
        public void handleMessage(Message msg) {
//            Log.i("mGetMessenger",msg.getData().getString("reply"));
        }
    };
    private static final Handler mProviderHandler  = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.i("mProviderHandler","msg");
        }
    };
    private final Handler mSocketHandler  = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.i("Socket",msg.obj.toString());
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPrintWriter.println("client send msg  " +
                            ""+ new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis())));
                }
            },5000);
        }
    };
    private static final Messenger mGetMessenger = new Messenger(mGetHandler);


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mRecipientConnection!=null){
            unbindService(mRecipientConnection);
        }
        if(mMessengerConnection!=null){
            unbindService(mMessengerConnection);
        }
        if(mIBookAidl!=null && mINewBookListener!=null){
            try {
                mIBookAidl.unregisterListener(mINewBookListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if(mContentResolver!=null){
            mContentResolver.unregisterContentObserver(mContentObserver);
        }
        if(mSocket!=null){
            try {
                mSocket.shutdownInput();
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
