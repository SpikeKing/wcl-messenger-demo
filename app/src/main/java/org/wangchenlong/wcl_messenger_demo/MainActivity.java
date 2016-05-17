package org.wangchenlong.wcl_messenger_demo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Messenger mMessenger;

    private Messenger mReplyMessenger;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder service) {
            mMessenger = new Messenger(service);
            Message msg = Message.obtain(null, ParasConsts.MSG_FROM_CLIENT);
            Bundle data = new Bundle();
            data.putString(ParasConsts.MSG_ARG, "Hello, I'm Spike, your friends.");
            msg.setData(data);

            // 需要设置Reply的Messenger.
            msg.replyTo = mReplyMessenger;

            try {
                mMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mReplyMessenger = new Messenger(new MessengerHandler(getApplicationContext()));
    }

    /**
     * 绑定服务的点击事件, 在首次绑定时, 可以通过Messenger进行通信
     *
     * @param view 按钮
     */
    public void bindService(View view) {
        Intent intent = new Intent(this, MessengerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 解除绑定, 未绑定会发生异常
     *
     * @param view 按钮
     */
    public void unbindService(View view) {
        try {
            unbindService(mConnection);
            Toast.makeText(view.getContext(), "解绑成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(view.getContext(), "未绑定", Toast.LENGTH_SHORT).show();
        }
    }

    @Override protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }

    private static class MessengerHandler extends Handler {
        private Context mContext;

        public MessengerHandler(Context context) {
            mContext = context.getApplicationContext();
        }

        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case ParasConsts.MSG_FROM_SERVICE:
                    String content = String.valueOf("客户端 - 收到信息: " + msg.getData().getString(ParasConsts.REPLY_ARG));
                    Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
}
