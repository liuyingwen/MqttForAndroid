package com.steven.mqtt;


import android.os.Handler;
import android.os.Message;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;

/**
 * author：steven
 * datetime：15/10/10 10:09
 * email：liuyingwen@kalading.com
 */
public abstract class MqttHandler extends Handler {
    public static final int HANDLE_MSG_CONNECTION_LOST = 0;
    public static final int HANDLE_MSG_DELIVERY_COMPLETE = 1;
    public static final int HANDLE_MSG_MESSAGE_ARRIVED = 2;
    public static final int HANDLE_MSG_CONNECTION_SUCCESS = 3;

    @Override
    public void dispatchMessage(Message msg) {
        Object o1 = msg.obj;
        switch (msg.what) {
            case HANDLE_MSG_CONNECTION_LOST:
                connectionLost((Throwable) o1);
                break;
            case HANDLE_MSG_DELIVERY_COMPLETE:
                deliveryComplete((IMqttDeliveryToken) o1);
                break;
            case HANDLE_MSG_MESSAGE_ARRIVED:
                try {
                    messageArrived((String) o1);
                } catch (Exception e) {
                }
                break;
            case HANDLE_MSG_CONNECTION_SUCCESS:
                connectSuccess();
                break;
        }
    }

    public abstract void connectionLost(Throwable t);

    public abstract void deliveryComplete(IMqttDeliveryToken token);

    public abstract void messageArrived(String message) throws Exception;

    public abstract void connectSuccess();
}
