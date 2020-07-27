package com.example.bttest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;


public class BleObject<mReceiver>
{
    private Context mContext;
    private BleListener mListener;
    private Handler mRssiReader;
    private Handler mRssiWatchdog;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;

    public interface BleListener
    {
        public void deviceFound(BluetoothDevice device);
        public void updateRssi(BluetoothDevice device, int rssi);
        // add more callbacks here, for example for discovered services
    }

    BleObject(Context context, BleListener listener)
    {
        mContext = context;
        mListener = listener;
        mRssiReader = new Handler();
        mRssiWatchdog = new Handler();
    }

    // this method should always be called before using BleObject
    void checkHardware()
    {
        BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        assert bluetoothManager != null;
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) return;
        mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    private BluetoothAdapter.LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback()
    {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord)
        {
            Log.d("vvv",device.getAddress());
            if (device == null) return;
            String address = device.getAddress();
            if (!BluetoothAdapter.checkBluetoothAddress(address)) return;
            mListener.deviceFound(device);
        }
    };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                readPeriodicalyRssi();
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                disconnect();
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status)
        {
            if (gatt == null)return;
            BluetoothDevice device = gatt.getDevice();
            if (device == null) return;
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                mListener.updateRssi(device, rssi);
            }
        }
    };

    private Runnable mWatchdogRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            mGattCallback.onReadRemoteRssi(mBluetoothGatt, -666, BluetoothGatt.GATT_SUCCESS);
        }
    };

    private Runnable mRssiRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            if (mBluetoothGatt == null) return;
            mBluetoothGatt.readRemoteRssi();
            readPeriodicalyRssi();
        }
    };

    private void readPeriodicalyRssi()
    {
        mRssiReader.postDelayed(mRssiRunnable, 5);
        mRssiWatchdog.removeCallbacks(mWatchdogRunnable);
        mRssiWatchdog.postDelayed(mWatchdogRunnable, 5000);
    }

    @SuppressWarnings("deprecation")
    public void startScanning()
    {
        mBluetoothAdapter.startDiscovery();
    }

    void bluetoothScanning()
    {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mReceiver, filter);
        mBluetoothAdapter.startDiscovery();
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                Log.i("vvv" , "device " + deviceName);
                Log.i("vvv " , "hard"  + deviceHardwareAddress);
            }
        }
    };


    @SuppressWarnings("deprecation")
    public void stopScanning()
    {
        mBluetoothAdapter.stopLeScan(mScanCallback);
    }

    void connect(String address)
    {
        connect(mBluetoothAdapter.getRemoteDevice(address));
    }

    void connect(final BluetoothDevice device)
    {
        Handler handler = new Handler();
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                mBluetoothGatt = device.connectGatt(mContext, true, mGattCallback);
            }
        });
    }

    void disconnect()
    {
        try
        {
            mBluetoothGatt.disconnect();
        }
        catch (Exception ignored){}

        try
        {
            mBluetoothGatt.close();
        }
        catch (Exception ignored){}
        mBluetoothGatt = null;
    }

}