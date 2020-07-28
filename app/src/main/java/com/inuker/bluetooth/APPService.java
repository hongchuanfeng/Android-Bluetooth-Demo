package com.inuker.bluetooth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.utils.ByteUtils;

import java.util.UUID;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;

public class APPService extends Service {

    private static final String packageName = "com.example.scangundemo_as";
    private static final String className = "APPService";
    private boolean isRuning = true;

    private String mMac;
    private UUID mService;
    private UUID mCharacter;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("Service","-------create-------");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Service","-------onDestroy-------");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Service","-----service onStartCommand...");
        mMac = intent.getStringExtra("mac");
        mService = (UUID) intent.getSerializableExtra("service");
        mCharacter = (UUID) intent.getSerializableExtra("character");

        new Thread(){
            @Override
            public void run() {
                super.run();

                while(isRuning){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    ClientManager.getClient().read(mMac, mService, mCharacter, mReadRsp);
                }
            }
        }.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final BleReadResponse mReadRsp = new BleReadResponse() {
        @Override
        public void onResponse(int code, byte[] data) {
            String msg = "";
            if (code == REQUEST_SUCCESS) {
//                mBtnRead.setText(String.format("读: %s", ByteUtils.byteToString(data)));
                msg = "成功......\n";
            } else {
//                CommonUtils.toast("失败");
//                mBtnRead.setText("读数据");
                msg = "失败......\n";
            }
            sendBrocast(msg);
        }
    };


    private void sendBrocast(String msg){
        Intent mIntent = new Intent(BluetoothBonedReceiver.ACTION_BONED);
        mIntent.putExtra(BluetoothBonedReceiver.ACTION_BONED_DATA, msg);
        sendBroadcast(mIntent);
    }


    @Override
    public boolean stopService(Intent name) {
        isRuning = false;
        return super.stopService(name);

    }
}
