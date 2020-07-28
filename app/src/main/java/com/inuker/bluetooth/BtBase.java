package com.inuker.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BtBase {

    //00001101-0000-1000-8000-00805F9B34FB  -  00001101-0000-1000-8000-00805f9b34fb
    //00001106-0000-1000-8000-00805F9B34FB  -  00001105-0000-1000-8000-00805f9b34fb
    //00001105-0000-1000-8000-00805f9B34FB  -  00001105-0000-1000-8000-00805f9b34fb
    //非手机终端的UUID
    public static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9B34FB");
    //手机终端的UUID
    public static final UUID MOB_UUID = UUID.fromString("00001105-0000-1000-8000-00805f9B34FB");
    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/bluetooth/";
    private static final int FLAG_MSG = 0;  //消息标记
    private static final int FLAG_FILE = 1; //文件标记

    private BluetoothSocket mSocket;
    private DataOutputStream mOut;
    private Listener mListener;
    public boolean isRead;
    public boolean isSending;

    BtBase(Listener listener) {
        mListener = listener;
    }

    /**
     * 循环读取对方数据(若没有数据，则阻塞等待)
     */
    void loopRead(BluetoothSocket socket) {
        mSocket = socket;
        try {
            Log.i("test","---------loopRead111--------");
            if (!mSocket.isConnected())
                mSocket.connect();
            Log.i("test","---------loopRead2222--------");
            notifyUI(Listener.CONNECTED, mSocket.getRemoteDevice());
            mOut = new DataOutputStream(mSocket.getOutputStream());
            DataInputStream in = new DataInputStream(mSocket.getInputStream());
            isRead = true;
            while (isRead) { //死循环读取
                Log.i("Test","------------------server in.readInt="+in.readInt());
                switch (in.readInt()) {
                    case FLAG_MSG: //读取短消息
                        String msg = in.readUTF();
                        notifyUI(Listener.MSG, "接收短消息：" + msg);
                        break;
                    case FLAG_FILE: //读取文件
                        Util.mkdirs(FILE_PATH);
                        String fileName = in.readUTF(); //文件名
                        long fileLen = in.readLong(); //文件长度
                        // 读取文件内容
                        long len = 0;
                        int r;
                        byte[] b = new byte[4 * 1024];
                        FileOutputStream out = new FileOutputStream(FILE_PATH + fileName);
                        notifyUI(Listener.MSG, "正在接收文件(" + fileName + "),请稍后...");
                        while ((r = in.read(b)) != -1) {
                            out.write(b, 0, r);
                            len += r;
                            if (len >= fileLen)
                                break;
                        }
                        notifyUI(Listener.MSG, "文件接收完成(存放在:" + FILE_PATH + ")");
                        break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Log.i("Test","-------------BtBase close-----------------");
            close();
        }
    }

    void loopRead2(BluetoothSocket socket) {
        mSocket = socket;
        try {
            Log.i("test","---------loopRead111--------");
            if (!mSocket.isConnected())
                mSocket.connect();
            Log.i("test","---------loopRead2222--------");
            notifyUI(Listener.CONNECTED, mSocket.getRemoteDevice());


        } catch (Throwable e) {
            e.printStackTrace();
            Log.i("Test","-------------BtBase close-----------------");
            close();
        }
    }

    /**
     * 循环读取对方数据(若没有数据，则阻塞等待)
     */
    void loopReadMsg() {

        try {

//            notifyUI(Listener.CONNECTED, mSocket.getRemoteDevice());
            Log.i("Test","-----------BtBase-loopReadMsg2---------");
//            mOut = new DataOutputStream(mSocket.getOutputStream());
            DataInputStream in = new DataInputStream(mSocket.getInputStream());
            isRead = true;
            Log.i("Test","-----------BtBase-loopReadMsg3---------");
            while (isRead) { //死循环读取
                String msg = in.readUTF();
                Log.i("Test","-----------BtBase-loopReadMsg4---------");
                notifyUI(Listener.MSG, "接收短消息：" + msg);

            }
        } catch (Throwable e) {
            e.printStackTrace();
            close();
        }
    }


    /**
     * 发送短消息
     */
    public void sendMsg2(String msg) {
        Log.i("Test","-----------sendMsg3----2-----");
        if (checkSend()) return;
        isSending = true;
        Log.i("Test","-----------sendMsg3----3-----");
        try {
            Log.i("Test","----------sendMsg2--mSocket="+mSocket);
            OutputStream out = mSocket.getOutputStream();
            Log.i("Test","----------sendMsg2--out="+out);
            mOut = new DataOutputStream(out);
            mOut.writeInt(FLAG_MSG); //消息标记
            mOut.writeUTF(msg);
            mOut.flush();
            notifyUI(Listener.MSG, "发送短消息：" + msg);
        } catch (Throwable e) {
            e.printStackTrace();
//            close();
        }
        isSending = false;
    }

    /**
     * 发送短消息
     */
    public void sendMsg(String msg) {
        Log.i("Test","-----------sendMsg3----2-----");
        if (checkSend()) return;
        isSending = true;
        Log.i("Test","-----------sendMsg3----3-----");
        try {

            mOut = new DataOutputStream(mSocket.getOutputStream());
            mOut.writeInt(FLAG_MSG); //消息标记
            mOut.writeUTF(msg);
            mOut.flush();
            notifyUI(Listener.MSG, "发送短消息：" + msg);
        } catch (Throwable e) {
            e.printStackTrace();
//            close();
        }
        isSending = false;
    }


    /**
     * 发送文件
     */
    public void sendFile(final String filePath) {
        if (checkSend()) return;
        isSending = true;
        Util.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FileInputStream in = new FileInputStream(filePath);
                    File file = new File(filePath);
                    mOut.writeInt(FLAG_FILE); //文件标记
                    mOut.writeUTF(file.getName()); //文件名
                    mOut.writeLong(file.length()); //文件长度
                    int r;
                    byte[] b = new byte[4 * 1024];
                    notifyUI(Listener.MSG, "正在发送文件(" + filePath + "),请稍后...");
                    while ((r = in.read(b)) != -1)
                        mOut.write(b, 0, r);
                    mOut.flush();
                    notifyUI(Listener.MSG, "文件发送完成.");
                } catch (Throwable e) {
                    close();
                }
                isSending = false;
            }
        });
    }

    /**
     * 释放监听引用(例如释放对Activity引用，避免内存泄漏)
     */
    public void unListener() {
        mListener = null;
    }

    /**
     * 关闭Socket连接
     */
    public void close() {
        try {
            Log.i("Test","---------关闭Socket连接-----------");
            isRead = false;
            if(mSocket!=null){
                mSocket.close();
                notifyUI(Listener.DISCONNECTED, null);
            }
//            notifyUI(Listener.DISCONNECTED, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 当前设备与指定设备是否连接
     */
    public boolean isConnected(BluetoothDevice dev) {
        boolean connected = (mSocket != null && mSocket.isConnected());
        if (dev == null)
            return connected;
        return connected && mSocket.getRemoteDevice().equals(dev);
    }

    // ============================================通知UI===========================================================
    public boolean checkSend() {
        if (isSending) {
            MyApplication.toast("正在发送其它数据,请稍后再发...", 0);
            return true;
        }
        return false;
    }

    public void notifyUI(final int state, final Object obj) {
        MyApplication.runUi(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mListener != null)
                        mListener.socketNotify(state, obj);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public interface Listener {
        int DISCONNECTED = 0;
        int CONNECTED = 1;
        int MSG = 2;

        void socketNotify(int state, Object obj);
    }

}
