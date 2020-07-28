package com.inuker.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class ComUtils {

    public void server(BluetoothDevice dev){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                InputStream is = null;
//                try {
//                    BluetoothServerSocket serverSocket = dev.listenUsingRfcommWithServiceRecord("serverSocket", uuid);
//                    mHandler.sendEmptyMessage(startService);
//                    BluetoothSocket accept = serverSocket.accept();
//                    is = accept.getInputStream();
//
//                    byte[] bytes = new byte[1024];
//                    int length = is.read(bytes);
//
//                    Message msg = new Message();
//                    msg.what = getMessageOk;
//                    msg.obj = new String(bytes, 0, length);
//                    mHandler.sendMessage(msg);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    public void client(){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                OutputStream os = null;
//                try {
//                    BluetoothSocket socket = strArr.get(i).createRfcommSocketToServiceRecord(uuid);
//                    socket.connect();
//                    os = socket.getOutputStream();
//                    os.write("testMessage".getBytes());
//                    os.flush();
//                    mHandler.sendEmptyMessage(sendOver);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }
}
