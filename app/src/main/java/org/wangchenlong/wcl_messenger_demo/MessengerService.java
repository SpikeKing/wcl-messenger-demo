package org.wangchenlong.wcl_messenger_demo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * 信使的服务端, 使用Messenger通信
 * <p/>
 * Created by wangchenlong on 16/5/9.
 */
public class MessengerService extends Service {

    private Messenger mMessenger;

    @Override public void onCreate() {
        super.onCreate();
        mMessenger = new Messenger(new MessengerHandler(getApplicationContext()));
    }

    @Nullable @Override public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    /**
     * 信使的持有, 处理返回信息
     */
    private static class MessengerHandler extends Handler {
        private Context mContext;

        public MessengerHandler(Context context) {
            mContext = context.getApplicationContext();
        }

        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case ParasConsts.MSG_FROM_CLIENT:

                    // 收到消息
                    String content = String.valueOf("服务端 - 收到消息: "
                            + msg.getData().getString(ParasConsts.MSG_ARG));
                    Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();

                    // 回复消息
                    Messenger client = msg.replyTo;
                    Message reply = Message.obtain(null, ParasConsts.MSG_FROM_SERVICE);
                    Bundle data = new Bundle();
                    data.putString(ParasConsts.REPLY_ARG, "消息已经收到");
                    reply.setData(data);

                    // 发生Reply的信息
                    try {
                        client.send(reply);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
