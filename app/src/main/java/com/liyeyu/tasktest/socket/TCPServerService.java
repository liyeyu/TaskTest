package com.liyeyu.tasktest.socket;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class TCPServerService extends Service {
    private AtomicBoolean mServiceAlive = new AtomicBoolean(true);
    private String[] mDefineMessages = new String[]{
            "你好，请问有什么需要帮助的？",
            "稍等，正在为你转向人工服务。",
            "暂忙，请稍后再拨。"
    };
    private Random mRandom;

    @Override
    public void onCreate() {
        super.onCreate();
        mRandom = new Random();
        new Thread(new TcpServer()).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mServiceAlive.set(false);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class TcpServer implements Runnable{
        @Override
        public void run() {
            ServerSocket serverSocket;
            try {
                serverSocket = new ServerSocket(8688);
                while(mServiceAlive.get()){
                    final Socket client = serverSocket.accept();
                    Log.i("Socket","accept");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            responseClient(client);
                        }
                    }).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void responseClient(Socket client){
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())),true);
            out.println("欢迎来到聊天室");
            while (mServiceAlive.get()){
                String line = in.readLine();
                Log.i("Socket",line);
                if(line==null){
                    break;
                }
                int i = mRandom.nextInt(mDefineMessages.length);
                String reMsg = mDefineMessages[i];
                out.println(reMsg);
            }
            Log.i("Socket","client close:"+client.getRemoteSocketAddress().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(in!=null){
                    in.close();
                }
                if(out!=null){
                    out.close();
                }
                client.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
