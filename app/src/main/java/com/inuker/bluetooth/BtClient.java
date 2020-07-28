package com.inuker.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class BtClient extends BtBase {

    private  BluetoothSocket mSocket;
    boolean flag = false;

    BtClient(Listener listener) {
        super(listener);
    }

    /**
     * 与远端设备建立长连接
     *
     * @param dev 远端设备
     */
    public void connect(BluetoothDevice dev) {
        close();
        try {

//             final BluetoothSocket socket = dev.createRfcommSocketToServiceRecord(SPP_UUID); //加密传输，Android系统强制配对，弹窗显示配对码
            final BluetoothSocket socket = dev.createInsecureRfcommSocketToServiceRecord(SPP_UUID); //明文传输(不安全)，无需配对
            Log.i("Test","------------------socket="+socket);
            // 开启子线程
            Util.EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("Test","-----------connect-------");
                    loopRead(socket); //循环读取

                }
            });
        } catch (Throwable e) {
            Log.i("Test","-------------BtClient close-----------------");
            close();
        }
    }


    public void connect2(BluetoothDevice dev,UUID uuid) {
        close();
        try {

//            ParcelUuid[] parcel = dev.getUuids();
//            UUID uuid = parcel[0].getUuid();
//             final BluetoothSocket socket = dev.createRfcommSocketToServiceRecord(SPP_UUID); //加密传输，Android系统强制配对，弹窗显示配对码
            final BluetoothSocket socket = dev.createInsecureRfcommSocketToServiceRecord(uuid); //明文传输(不安全)，无需配对
            Log.i("Test","------------------socket="+socket);
            // 开启子线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean isCon = socket.isConnected();
                        Log.i("Test","------------------isCon="+isCon);
                        socket.connect();
                        loopRead2(socket); //循环读取


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();

        } catch (Throwable e) {
            Log.i("Test","-------------BtClient close-----------------");
            close();
        }
    }



    public void connect3(BluetoothDevice dev) {
        close();
        try {

//            ParcelUuid[] parcel = dev.getUuids();
//            UUID uuid = parcel[0].getUuid();
//             final BluetoothSocket socket = dev.createRfcommSocketToServiceRecord(SPP_UUID); //加密传输，Android系统强制配对，弹窗显示配对码
            final BluetoothSocket socket = dev.createInsecureRfcommSocketToServiceRecord(SPP_UUID); //明文传输(不安全)，无需配对
            Log.i("Test","------------------socket="+socket);
            // 开启子线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean isCon = socket.isConnected();
                        Log.i("Test","------------------isCon="+isCon);
                        socket.connect();
                        loopRead2(socket); //循环读取


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();

        } catch (Throwable e) {
            Log.i("Test","-------------BtClient close-----------------");
            close();
        }
    }



    public void loopReadMsg(BluetoothDevice dev) {
//        close();
        try {
            Log.i("Test","-----------loopReadMsg1---------");
//             final BluetoothSocket socket = dev.createRfcommSocketToServiceRecord(SPP_UUID); //加密传输，Android系统强制配对，弹窗显示配对码
//            final BluetoothSocket socket = dev.createInsecureRfcommSocketToServiceRecord(SPP_UUID); //明文传输(不安全)，无需配对
            // 开启子线程
            Util.EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    Log.i("Test","-----------loopReadMsg2---------");
                    loopReadMsg(); //循环读取
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            Log.i("Test","-----------loopReadMsg3---------ex="+e.getMessage());
            close();
        }
    }


    public void clientSendMsg(String msg) {
        Log.i("Test","-----------sendMsg3----2-----");
        if (checkSend()) return;
        isSending = true;
        Log.i("Test","-----------sendMsg3----3-----");
        try {

            Log.i("Test","----------sendMsg2--mSocket="+mSocket);
            OutputStream out = mSocket.getOutputStream();
            Log.i("Test","----------sendMsg2--out="+out);
            DataOutputStream mOut = new DataOutputStream(out);
            mOut.writeInt(Listener.MSG); //消息标记
            mOut.writeUTF(msg);
            mOut.flush();
            notifyUI(Listener.MSG, "发送短消息：" + msg);
        } catch (Throwable e) {
            e.printStackTrace();
//            close();
        }
        isSending = false;
    }




}
