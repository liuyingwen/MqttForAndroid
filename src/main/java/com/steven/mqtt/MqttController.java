package com.steven.mqtt;

import android.os.Message;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * author：steven
 * datetime：15/10/10 10:08
 * email：liuyingwen@kalading.com
 */
public class MqttController {
    private MqttCallback mMqttCallback;
    private MqttClient mClient;
    private MqttConnectOptions mOptions;
    private ScheduledExecutorService mScheduledExecutorService;
    private String mHost;
    private MqttHandler mMqttHandler;
    private String mDeviceId;
    private String mTopic;

    public MqttController(String host, String deviceId, String topic, MqttHandler mqttHandler) {
        mHost = host;
        mMqttHandler = mqttHandler;
        mDeviceId = deviceId;
        mTopic = topic;
    }

    public void init() {
        System.out.println("初始化");
        try {
            mClient = new MqttClient(mHost, mDeviceId, new MemoryPersistence());
            mOptions = new MqttConnectOptions();
            mOptions.setCleanSession(true);
            mOptions.setConnectionTimeout(10);
            mOptions.setKeepAliveInterval(20);
            mClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("connectionLost----------");
                    startReconnect();
                    if (mMqttHandler != null) {
                        Message msg = mMqttHandler.obtainMessage();
                        msg.what = MqttHandler.HANDLE_MSG_CONNECTION_LOST;
                        msg.obj = cause;
                        mMqttHandler.sendMessage(msg);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("deliveryComplete---------" + token.isComplete());
                    if (mMqttHandler != null) {
                        Message msg = mMqttHandler.obtainMessage();
                        msg.what = MqttHandler.HANDLE_MSG_DELIVERY_COMPLETE;
                        msg.obj = token;
                        mMqttHandler.sendMessage(msg);
                    }
                }

                @Override
                public void messageArrived(String topicName, MqttMessage message)
                        throws Exception {
                    if (mMqttHandler != null && message != null) {
                        System.out.println("messageArrived----------" + message.toString());
                        Message msg = mMqttHandler.obtainMessage();
                        msg.what = MqttHandler.HANDLE_MSG_MESSAGE_ARRIVED;
                        msg.obj = message.toString();
                        mMqttHandler.sendMessage(msg);
                    }
                }
            });
        } catch (Exception e) {
            System.out.println("error ------ " + e.getMessage());
        }
    }

    public void startReconnect() {
        mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!mClient.isConnected()) {
                    connect();
                }
            }
        }, 0, 10 * 1000, TimeUnit.MILLISECONDS);
    }

    public void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mClient.connect(mOptions);
                    mClient.subscribe(mTopic, 0);
                    if (mMqttHandler != null) {
                        Message msg = mMqttHandler.obtainMessage();
                        msg.what = MqttHandler.HANDLE_MSG_CONNECTION_SUCCESS;
                        mMqttHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    System.out.println("error1 -------- " + e.getMessage());
                }
            }
        }).start();
    }

    public void sendMessage(String message) throws MqttException {
        if (mClient != null && mClient.isConnected()) {
            mClient.publish(mTopic, message.getBytes(), 1, true);
        }
    }

    public void destory() {
        try {
            mScheduledExecutorService.shutdown();
            mClient.disconnect();
        } catch (MqttException e) {
        }
    }

    public boolean isConnect() {
        return mClient.isConnected();
    }
}
