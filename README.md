# Android 进程使用 Messenger 通信

> 欢迎探讨技术, Follow我的GitHub: https://github.com/SpikeKing 

进程之间不能共享内存数据, 但是可以进行通信, 除了简单的[Intent通信](http://www.wangchenlong.org/2016/05/07/1605/072-serial-object/), 也可以使用Messenger, Messenger基于AIDL实现, 顺序执行, 不支持并发. 为了区分通信的始末, 我们暂定发送数据是客户端, 接收数据是服务端. 本文介绍Messenger的使用方式, 含有[Demo](https://github.com/SpikeKing/wcl-messenger-demo).

![Messenger](https://raw.githubusercontent.com/SpikeKing/wcl-messenger-demo/master/article/messenger.png)

本文源码的GitHub[下载地址](https://github.com/SpikeKing/wcl-messenger-demo)

---

## 客户端

客户端发送数据到服务端, 服务端收到数据反馈回客户端.

### 接收反馈数据

MainActivity作为客户端, 发送信息. 首先创建消息的Handler类, 用于接收服务端的反馈, 继承``Handler``, 重写``handleMessage``方法, ``msg.what``类型, ``msg.getData()``数据.

``` java
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
```

使用Handler创建Messenger.

``` java
mReplyMessenger = new Messenger(new MessengerHandler(getApplicationContext()));
// ...
msg.replyTo = mReplyMessenger;
```

### 连接服务发送数据

创建``ServiceConnection``类, 实现``onServiceConnected``方法. 创建信使``Messenger``, 创建消息``Message``, 在Message中添加序列化数据``msg.setData()``, 设置接收反馈``msg.replyTo``. Messenger发送数据``send``.

``` java
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
```

> 注意信使: Messenger, 消息: Message, 拼写略有不同.

### 绑定服务

添加Connection, 使用``Context.BIND_AUTO_CREATE``, 绑定自动创建.

``` java
public void bindService(View view) {
    Intent intent = new Intent(this, MessengerService.class);
    bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
}
```

解绑服务unbindService.

``` java
public void unbindService(View view) {
    try {
        unbindService(mConnection);
        Toast.makeText(view.getContext(), "解绑成功", Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
        e.printStackTrace();
        Toast.makeText(view.getContext(), "未绑定", Toast.LENGTH_SHORT).show();
    }
}
```

> 绑定服务一定需要解绑服务, 防止泄露. 如果没有注册, 解绑会发生异常.

## 服务端

服务端负责接收数据, 收到给客户端反馈.

**MessengerService**继承Service, 显示客户端消息``msg.getData()``. 反馈信息的``Messenger``使用客户端传递的, 创建消息添加内容, 使用客户端的Messenger传递给客户端.

### 处理与反馈数据

``` java
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
```

### 绑定接收数据

使用Handler创建服务端的Messenger

```java
mMessenger = new Messenger(new MessengerHandler(getApplicationContext()));
```

绑定Handler, 与客户端交流.

``` java
@Nullable @Override public IBinder onBind(Intent intent) {
    return mMessenger.getBinder();
}
```

> 默认返回null.

---

客户端, 使用Messenger传递消息Message, Message中添加序列化数据Bundle; 服务端, 使用Handler解析获取的Message, 通过辨别类型, 获取数据. Messenger使用非常明晰, 易于控制, 是简单进程通信的首选.
