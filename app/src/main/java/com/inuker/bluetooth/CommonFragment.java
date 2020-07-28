package com.inuker.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.inuker.bluetooth.view.PullRefreshListView;
import com.inuker.bluetooth.view.PullToRefreshFrameLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;


public class CommonFragment extends Fragment implements BtBase.Listener, BtReceiver.Listener, BtDevAdapter.Listener{

    private TextView mTips;
    private EditText mInputMsg;
    private EditText mInputFile;
    private TextView mLogs;
    private BtReceiver mBtReceiver;
    private  BtDevAdapter mBtDevAdapter;
    private  BtClient mClient;

    private View view;
    private Context context;

    private Button scanBtn,sendMsgBtn,sendFileBtn,listenTerBtn,listenMobBtn;
    private BluetoothDevice remoteDev;
    private boolean isLoop = true;
    private BtServer mServer;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private boolean isListen = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_common, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        view = getView();
        context = view.getContext();
        mBtDevAdapter = new BtDevAdapter(this);
        mClient = new BtClient(this);
        mServer = new BtServer(this);

        listenTerBtn = view.findViewById(R.id.listen_ter);
        listenMobBtn = view.findViewById(R.id.listen_mob);


        RecyclerView rv = view.findViewById(R.id.rv_bt);
        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.setAdapter(mBtDevAdapter);
        mTips = view.findViewById(R.id.tv_tips);
        mInputMsg = view.findViewById(R.id.input_msg);
        mInputFile = view.findViewById(R.id.input_file);
        mLogs = view.findViewById(R.id.tv_log);

        scanBtn = view.findViewById(R.id.button2);
        sendMsgBtn = view.findViewById(R.id.btn_sendMsg);
        sendFileBtn = view.findViewById(R.id.btn_sendFile);
        mBtReceiver = new BtReceiver(context, this);//注册蓝牙广播
        BluetoothAdapter.getDefaultAdapter().startDiscovery();


        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reScan();
            }
        });

        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String txt = sendMsgBtn.getText().toString();
                Log.i("Test","-----is="+"发送信息".equals(txt));
                if("发送信息".equals(txt)){
//                    handler3.sendEmptyMessage(0);
                    mLogs.setText("");
                        sendMsgBtn.setText("关闭");
//                        sendMsg11();
//                        isLoop = true;
//
                    loopSendMsg();


                }else{
//                        isLoop = false;
//                        sendMsgBtn.setText("发送信息");
                    handler3.sendEmptyMessage(1);
                }

            }
        });

        sendFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFile();
            }
        });

        listenTerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mServer.close();
                mServer.listen(BtBase.SPP_UUID);
                isListen = true;
            }
        });

        listenMobBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mServer.close();
                mServer.listen(BtBase.MOB_UUID);
                isListen = true;
            }
        });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        context.unregisterReceiver(mBtReceiver);
        mClient.unListener();
        mClient.close();

        mServer.unListener();
        mServer.close();
    }


    @Override
    public void socketNotify(int state, Object obj) {
//        if (isDestroyed())
//            return;
        String msg = null;
        switch (state) {
            case BtBase.Listener.CONNECTED:
                BluetoothDevice dev = (BluetoothDevice) obj;
                msg = String.format("与%s(%s)连接成功", dev.getName(), dev.getAddress());
                remoteDev = dev;
                mTips.setText(msg);
                break;
            case BtBase.Listener.DISCONNECTED:
                msg = "连接断开";
                mTips.setText(msg);
                break;
            case BtBase.Listener.MSG:
                msg = String.format("\n%s", obj);
                mLogs.append(msg);

                break;
        }
        MyApplication.toast(msg, 0);
    }

    @Override
    public void onItemClick(BluetoothDevice dev) {

        mServer.close();
        mBluetoothAdapter.cancelDiscovery();

        Log.i("Test","------name="+dev.getName()+","+dev.getAddress());
        if (mClient.isConnected(dev)) {
            remoteDev = dev;
            MyApplication.toast("已经连接了", 0);
            return;
        }

        int state = dev.getBondState();
        if(state != BluetoothDevice.BOND_BONDED){
            boolean isPair = BluetoothUtils.pair(dev.getAddress(),"");
//
            Log.i("Test","-------------------isPair="+isPair);
            MyApplication.toast("请先配对蓝牙", 0);
            reScan();
            return;
        }

        ParcelUuid[] parcel = dev.getUuids();

        boolean isContain = isContainUUID(dev);
        if(isContain){
            mServer.listen(BtBase.SPP_UUID);
            mClient.connect2(dev,BtBase.SPP_UUID);
        }else{
            mServer.listen(BtBase.MOB_UUID);
            mClient.connect2(dev,BtBase.MOB_UUID);

        }

        MyApplication.toast("正在连接...", 0);
        mTips.setText("正在连接...");
    }

    @Override
    public void foundDev(BluetoothDevice dev) {
        mBtDevAdapter.add(dev);
    }


    // 重新扫描
    public void reScan() {
        mBtDevAdapter.reScan();
    }

    //发送信息
    public void sendMsg2() {
        if (mClient.isConnected(null)) {
            String msg = mInputMsg.getText().toString();
            if (TextUtils.isEmpty(msg))
                MyApplication.toast("消息不能空", 0);
            else
                mClient.sendMsg(msg);
        } else
            MyApplication.toast("没有连接", 0);
    }

    public void sendMsgByClient() {

            mClient.sendMsg("发送成功");

    }


    //发送信息
    public void loopReadMsg() {
        if (mClient.isConnected(null)) {
            String msg = "发送成功......";
            mClient.loopReadMsg();

        } else
            MyApplication.toast("没有连接", 0);
    }


    public void sendMsg() {
        Log.i("Test","-----------sendMsg1---------");
        if(remoteDev == null){
            MyApplication.toast("请选连接蓝牙", 0);
            return;
        }
        Log.i("Test","-----------sendMsg2---------");
        if (!mClient.isConnected(remoteDev)) {
            MyApplication.toast("请选连接蓝牙", 0);
            return;
        }
        Log.i("Test","-----------sendMsg3---------");
        mClient.loopReadMsg(remoteDev);
    }


    //发送文件
    public void sendFile() {
        if (mClient.isConnected(null)) {
            String filePath = mInputFile.getText().toString();
            if (TextUtils.isEmpty(filePath) || !new File(filePath).isFile())
                MyApplication.toast("文件无效", 0);
            else
                mClient.sendFile(filePath);
        } else
            MyApplication.toast("没有连接", 0);
    }

    private void loopRead(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isLoop){
//                    sendMsg3();
                    loopReadMsg();
                }
            }
        }).start();
    }

    private Handler handler = new Handler(){

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String txt = sendMsgBtn.getText().toString();
            if("发送信息".equals(txt)){
//                    loopRead();
//                loopRead();
                if (mClient.isConnected(null)) {
//                    String msg = "发送成功......";
                    sendMsgBtn.setText("关闭");
                    mClient.loopReadMsg();

                    isLoop = true;

                } else
                    MyApplication.toast("没有连接", 0);

            }else{
                isLoop = false;
                sendMsgBtn.setText("发送信息");
            }

        }
    };


    public void serverSendMsg() {
        if (mServer.isConnected(null)) {
//            String msg = mInputMsg.getText().toString();
//            if (TextUtils.isEmpty(msg))
//                MyApplication.toast("消息不能空", 0);
//            else
                mServer.sendMsg("测试。。。。");
        } else
            MyApplication.toast("没有连接", 0);
    }

    private boolean isContainUUID(BluetoothDevice dev){
        ParcelUuid[] pars = dev.getUuids();
        for(ParcelUuid p : pars){
            UUID u = p.getUuid();
            if(BtBase.SPP_UUID.equals(u)){
                return true;
            }
        }
        return false;
    }


    private Handler handler3 = new Handler(){

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
//                    sendMsgBtn.setText("关闭");
                    sendMsgByClient();
                    isLoop = true;
                    break;
                case 1:
                    isLoop = false;
                    sendMsgBtn.setText("发送信息");
                    break;
            }

        }
    };


    private void loopSendMsg(){
        new Thread(new Runnable() {
            @Override
            public void run() {

                while(isLoop){
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler3.sendEmptyMessage(0);

                }
            }
        }).start();
    }
}