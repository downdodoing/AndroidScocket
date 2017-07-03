package com.example.mvp.myapplicationtest;

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

public class TCPServerService extends Service {

    public static final String TAG = "TCPServerService";
    private boolean mIsServiceDestroyed = false;
    private String[] mDefinedMessages = new String[]{
            "你好啊！",
            "请问你叫神马名字",
            "今天天气还是很炎热的",
            "你知道吗？我可是可以和很多人一起聊天的",
            "给你讲个笑话吧，据说........"};
    @Override
    public void onCreate() {
        new Thread(new TcpServer()).start();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mIsServiceDestroyed = true;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class TcpServer implements Runnable {
        @Override
        public void run() {
            ServerSocket socket = null;
            try {
                //监听本地8688端口
                socket = new ServerSocket(8688);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            while (!mIsServiceDestroyed) {
                //接收客户端数据请求
                try {
                    final Socket client = socket.accept();
                    Log.i(TAG, "accept");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                responseClient(client);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void responseClient(Socket client) throws IOException {
            //用于接收客户端消息
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            //用于向客户端发送消息
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
            out.println("欢迎来到聊天室");
            while (!mIsServiceDestroyed) {
                String str = in.readLine();
                Log.i(TAG, "responseClient: " + str);
                if (null == str) {
                    break;
                }
                int i = new Random().nextInt(mDefinedMessages.length);
                String message = mDefinedMessages[i];
                out.println(message);
                Log.i(TAG, "sendMessage: " + message);
            }
            Log.i(TAG, "clientQuit");
            out.close();
            in.close();
            client.close();
        }
    }
}
