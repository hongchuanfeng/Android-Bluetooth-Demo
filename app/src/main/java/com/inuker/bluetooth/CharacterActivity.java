package com.inuker.bluetooth;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleMtuResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.connect.response.BleUnnotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.inuker.bluetooth.library.utils.ByteUtils;

import static com.inuker.bluetooth.library.Constants.*;

import java.util.UUID;

/**
 * Created by dingjikerbo on 2016/9/6.
 */
public class CharacterActivity extends Activity implements View.OnClickListener {

    private String mMac;
    private UUID mService;
    private UUID mCharacter;

    private TextView mTvTitle;

    private Button mBtnRead;

    private Button mBtnWrite;
    private EditText mEtInput;

    private Button mBtnNotify;
    private Button mBtnUnnotify;
    private EditText mEtInputMtu;
    private Button mBtnRequestMtu;
    private TextView textView;
    private Intent regIntent;


    private boolean isLoop = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.character_activity);

        Intent intent = getIntent();
        mMac = intent.getStringExtra("mac");
        mService = (UUID) intent.getSerializableExtra("service");
        mCharacter = (UUID) intent.getSerializableExtra("character");

        mTvTitle = (TextView) findViewById(R.id.title);
        mTvTitle.setText(String.format("%s", mMac));

        mBtnRead = (Button) findViewById(R.id.read);

        mBtnWrite = (Button) findViewById(R.id.write);
        mEtInput = (EditText) findViewById(R.id.input);

        mBtnNotify = (Button) findViewById(R.id.notify);
        mBtnUnnotify = (Button) findViewById(R.id.unnotify);

        mEtInputMtu = (EditText) findViewById(R.id.et_input_mtu);
        mBtnRequestMtu = (Button) findViewById(R.id.btn_request_mtu);

        textView = findViewById(R.id.textView);

        mBtnRead.setOnClickListener(this);
        mBtnWrite.setOnClickListener(this);

        mBtnNotify.setOnClickListener(this);
        mBtnNotify.setEnabled(true);

        mBtnUnnotify.setOnClickListener(this);
        mBtnUnnotify.setEnabled(false);

        mBtnRequestMtu.setOnClickListener(this);

        IntentFilter intentFilter1 = new IntentFilter();
        // 监视蓝牙关闭和打开的状态
        intentFilter1.addAction(BluetoothBonedReceiver.ACTION_BONED);
        registerReceiver(bonedReceiver, intentFilter1);

    }

    private final BleReadResponse mReadRsp = new BleReadResponse() {
        @Override
        public void onResponse(int code, byte[] data) {
            if (code == REQUEST_SUCCESS) {
//                mBtnRead.setText(String.format("读: %s", ByteUtils.byteToString(data)));
//                CommonUtils.toast("成功");
                Message msg = new Message();
                msg.obj = "成功......\n";
                handler.sendMessage(msg);
            } else {
//                CommonUtils.toast("失败");
//                mBtnRead.setText("读数据");
                Message msg = new Message();
                msg.obj = "失败......\n";
                handler.sendMessage(msg);
            }
        }
    };

    private final BleWriteResponse mWriteRsp = new BleWriteResponse() {
        @Override
        public void onResponse(int code) {
            if (code == REQUEST_SUCCESS) {
                CommonUtils.toast("成功");
            } else {
                CommonUtils.toast("失败");
            }
        }
    };

    private final BleNotifyResponse mNotifyRsp = new BleNotifyResponse() {
        @Override
        public void onNotify(UUID service, UUID character, byte[] value) {
            if (service.equals(mService) && character.equals(mCharacter)) {
                mBtnNotify.setText(String.format("%s", ByteUtils.byteToString(value)));
            }
        }

        @Override
        public void onResponse(int code) {
            if (code == REQUEST_SUCCESS) {
                mBtnNotify.setEnabled(false);
                mBtnUnnotify.setEnabled(true);
                CommonUtils.toast("成功");
            } else {
                CommonUtils.toast("失败");
            }
        }
    };

    private final BleUnnotifyResponse mUnnotifyRsp = new BleUnnotifyResponse() {
        @Override
        public void onResponse(int code) {
            if (code == REQUEST_SUCCESS) {
                CommonUtils.toast("成功");
                mBtnNotify.setEnabled(true);
                mBtnUnnotify.setEnabled(false);
            } else {
                CommonUtils.toast("失败");
            }
        }
    };

    private final BleMtuResponse mMtuResponse = new BleMtuResponse() {
        @Override
        public void onResponse(int code, Integer data) {
            if (code == REQUEST_SUCCESS) {
                CommonUtils.toast("请求 mtu 成功,mtu = " + data);
            } else {
                CommonUtils.toast("请求 mtu 失败");
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.read:
                String txt = mBtnRead.getText().toString().toLowerCase();
                Log.i("Test","-------txt-----"+txt);
                if("ping".equals(txt)){
                    mBtnRead.setText("关闭");
                    Log.i("Test","-------read-----");
//                    regIntent = new Intent(this, APPService.class);
//                    regIntent.setAction("android.intent.action.RESPOND_VIA_MESSAGE");
//                    regIntent.putExtra("mac", mMac);
//                    regIntent.putExtra("service", mService);
//                    regIntent.putExtra("character", mCharacter);
//                    startService(regIntent);
                    loopSendMsg();


                }else{
//                    if(regIntent!=null){
//                        stopService(regIntent);
//                    }
                    isLoop = false;
                    mBtnRead.setText("PING");

                }
//                ClientManager.getClient().read(mMac, mService, mCharacter, mReadRsp);


                break;
            case R.id.write:
                ClientManager.getClient().write(mMac, mService, mCharacter,
                        ByteUtils.stringToBytes(mEtInput.getText().toString()), mWriteRsp);
                break;
            case R.id.notify:
                ClientManager.getClient().notify(mMac, mService, mCharacter, mNotifyRsp);
                break;
            case R.id.unnotify:
                ClientManager.getClient().unnotify(mMac, mService, mCharacter, mUnnotifyRsp);
                break;
            case R.id.btn_request_mtu:
                String mtuStr = mEtInputMtu.getText().toString();
                if (TextUtils.isEmpty(mtuStr)) {
                    CommonUtils.toast("MTU不能为空");
                    return;
                }
                int mtu = Integer.parseInt(mtuStr);
                if (mtu < GATT_DEF_BLE_MTU_SIZE || mtu > GATT_MAX_MTU_SIZE) {
                    CommonUtils.toast("MTU不不在范围内");
                    return;
                }
                ClientManager.getClient().requestMtu(mMac, mtu, mMtuResponse);
                break;
        }
    }

    private final BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            BluetoothLog.v(String.format("CharacterActivity.onConnectStatusChanged status = %d", status));

            if (status == STATUS_DISCONNECTED) {
                CommonUtils.toast("无连接蓝牙");
                mBtnRead.setEnabled(false);
                mBtnWrite.setEnabled(false);
                mBtnNotify.setEnabled(false);
                mBtnUnnotify.setEnabled(false);

                mTvTitle.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        finish();
                    }
                }, 300);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        ClientManager.getClient().registerConnectStatusListener(mMac, mConnectStatusListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ClientManager.getClient().unregisterConnectStatusListener(mMac, mConnectStatusListener);
    }

    private BroadcastReceiver bonedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothBonedReceiver.ACTION_BONED.equals(action)){
                String data = intent.getStringExtra(BluetoothBonedReceiver.ACTION_BONED_DATA);
                Message msg = new Message();
                msg.obj = data;
                handler.sendMessage(msg);
            }
        }
    };

    private Handler handler = new Handler(){

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
//            tv.setText("");
            String txt = textView.getText().toString();
            textView.setText(msg.obj+""+txt);
        }
    };

    private void before(){
        mBtnRead.setText("关闭");
        Log.i("Test","-------read-----");
        regIntent = new Intent(this, APPService.class);
        regIntent.setAction("android.intent.action.RESPOND_VIA_MESSAGE");
        regIntent.putExtra("mac", mMac);
        regIntent.putExtra("service", mService);
        regIntent.putExtra("character", mCharacter);
        startService(regIntent);
    }

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

    private Handler handler3 = new Handler(){

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
//                    sendMsgBtn.setText("关闭");
//                    sendMsg11();
                    isLoop = true;
                    ClientManager.getClient().read(mMac, mService, mCharacter, mReadRsp);
                    break;
                case 1:
                    isLoop = false;
//                    sendMsgBtn.setText("发送信息");
                    break;
            }

        }
    };



}
