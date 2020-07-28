package com.inuker.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.UUID;

public class BtServer extends BtBase{

    private static final String TAG = BtServer.class.getSimpleName();
    private BluetoothServerSocket mSSocket;
    private BluetoothSocket socket;

    BtServer(Listener listener) {
        super(listener);
//        listen();
    }

    /**
     * 监听客户端发起的连接
     */
    public void listen() {
        try {
//            Method listenMethod = btClass.getMethod("listenUsingRfcommOn", new Class[]{int.class});

            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
//            mSSocket = adapter.listenUsingRfcommWithServiceRecord(TAG, SPP_UUID); //加密传输，Android强制执行配对，弹窗显示配对码
            mSSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(TAG, MOB_UUID); //明文传输(不安全)，无需配对
            // 开启子线程
            Util.EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                         socket = mSSocket.accept(); // 监听连接
//                        mSSocket.close(); // 关闭监听，只连接一个设备
                        loopRead(socket); // 循环读取
                    } catch (Throwable e) {
                        close();
                    }
                }
            });
        } catch (Throwable e) {
            close();
        }
    }

    public void listen(UUID uuid) {
        try {
//            Method listenMethod = btClass.getMethod("listenUsingRfcommOn", new Class[]{int.class});
            Log.i("Test","--------------------uuid="+uuid);
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
//            mSSocket = adapter.listenUsingRfcommWithServiceRecord(TAG, SPP_UUID); //加密传输，Android强制执行配对，弹窗显示配对码
            mSSocket = adapter.listenUsingInsecureRfcommWithServiceRecord(TAG, uuid); //明文传输(不安全)，无需配对
            // 开启子线程
            Util.EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                         socket = mSSocket.accept(); // 监听连接
//                        mSSocket.close(); // 关闭监听，只连接一个设备
                        loopRead(socket); // 循环读取
                    } catch (Throwable e) {
                        close();
                    }
                }
            });
        } catch (Throwable e) {
            close();
        }
    }

    @Override
    public void close() {
        super.close();
        try {
            if(socket!=null){
                socket.close();
            }

            if(mSSocket!=null){
                mSSocket.close();
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
