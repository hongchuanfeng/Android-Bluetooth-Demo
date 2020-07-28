package com.inuker.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.lang.reflect.Method;

public class BluetoothUtils {

    public static boolean pair(String strAddr, String strPsw)
    {
//        Log.i("Test","------------pair--mac="+strAddr);
//        boolean result = false;
//        //蓝牙设备适配器
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//
//        //取消发现当前设备的过程
//        bluetoothAdapter.cancelDiscovery();
//
//        if (!bluetoothAdapter.isEnabled())
//        {
//            bluetoothAdapter.enable();
//        }
//        if (!BluetoothAdapter.checkBluetoothAddress(strAddr))
//        { // 检查蓝牙地址是否有效
//            Log.i("Test", "蓝牙地址无效!");
//        }

        //由蓝牙设备地址获得另一蓝牙设备对象
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(strAddr);

        try
        {


            Log.i("Test", "-------------未配对");
            ClsUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
            boolean isBond = ClsUtils.createBond(device.getClass(), device);
            Log.i("Test", "-------------isBond1="+isBond);
//            boolean inputState = ClsUtils.cancelPairingUserInput(device.getClass(),device);
//            ClsUtils.cancelBondProcess(device.getClass(),device);
//            Log.i("Test", "-------------inputState="+inputState);

            return isBond;
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            Log.i("Test", "------------ex1="+e);
            e.printStackTrace();
        } //

        return false;
    }

    public static boolean pairDevice(String strAddr, String strPsw)
    {
        Log.i("Test","------------pair--mac="+strAddr);
        boolean result = false;
        //蓝牙设备适配器
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //取消发现当前设备的过程
        bluetoothAdapter.cancelDiscovery();

        if (!bluetoothAdapter.isEnabled())
        {
            bluetoothAdapter.enable();
        }
        if (!BluetoothAdapter.checkBluetoothAddress(strAddr))
        { // 检查蓝牙地址是否有效
            Log.i("Test", "蓝牙地址无效!");
        }

        //由蓝牙设备地址获得另一蓝牙设备对象
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(strAddr);

        if (device.getBondState() != BluetoothDevice.BOND_BONDED)
        {
            try
            {
                Log.i("Test", "-------------未配对");
                ClsUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
                boolean isBond = ClsUtils.createBond(device.getClass(), device);
                Log.i("Test", "-------------isBond1="+isBond);

                return isBond;
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                Log.i("Test", "------------ex1="+e);
                e.printStackTrace();
            } //

        }
        else
        {
            Log.i("Test", "------------已经配对");

            try
            {
                //ClsUtils这个类的的以下静态方法都是通过反射机制得到需要的方法
                ClsUtils.createBond(device.getClass(), device);//创建绑定
                ClsUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
                boolean isbond = ClsUtils.createBond(device.getClass(), device);
                //    ClsUtils.cancelPairingUserInput(device.getClass(), device);
//                remoteDevice = device; // 如果绑定成功，就直接把这个设备对象传给全局的remoteDevice
                result = true;
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                Log.d("test", "--------------ex2="+e);
                e.printStackTrace();
            }
        }
        return result;
    }

    //通过反射来调用BluetoothDevice.removeBond取消设备的配对
    public static boolean unpair(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.setAccessible(true);
            boolean state = (boolean)m.invoke(device, (Object[]) null);
            Log.i("Test","-------------unpair----state="+state);
            return state;
        } catch (Exception e) {
            Log.e("ble",e.toString());
        }
        return false;
    }


}
