package com.liyeyu.tasktest;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class MessengerService extends Service {
    public MessengerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    public class MessengerHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
//            Log.i("MessengerHandler","service receiver msg:"+msg.getData().getString("msg"));
            Messenger messenger = msg.replyTo;
            Message msg1 = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString("reply","service send msg 111");
            msg1.setData(bundle);
            try {
                messenger.send(msg1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private final Messenger mMessenger = new Messenger(new MessengerHandler());
}
