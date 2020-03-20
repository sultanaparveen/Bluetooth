package com.example.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private lateinit var RFIDReader: BluetoothDevice
    private lateinit var deviceName: TextView
    private lateinit var deviceAddress: TextView

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        deviceName = tvDeviceName
        deviceAddress = tvDeviceAddress
        //Broadcasts when bond state changes (ie:pairing)
        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(mBroadcastReceiver2, filter)
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private fun checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            var permissionCheck = checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION")
            permissionCheck += checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION")
            if (permissionCheck != 0) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1001) //Any number
            }
        } else {
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.")
        }
    }

    fun btnDiscover(view: View?) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.")
        if (mBluetoothAdapter!!.isDiscovering) {
            mBluetoothAdapter!!.cancelDiscovery()
            Log.d(TAG, "btnDiscover: Canceling discovery.")
            //check BT permissions in manifest
            checkBTPermissions()
            mBluetoothAdapter!!.startDiscovery()
            val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(mBroadcastReceiver1, discoverDevicesIntent)
        }
        if (!mBluetoothAdapter!!.isDiscovering) { //check BT permissions in manifest
            checkBTPermissions()
            mBluetoothAdapter!!.startDiscovery()
            val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(mBroadcastReceiver1, discoverDevicesIntent)
        }
    }

    fun pair() {
        mBluetoothAdapter!!.cancelDiscovery()
        Log.d(TAG, "onItemClick: You Clicked on a device.")
        Log.d(TAG, "onItemClick: deviceName = $deviceName")
        Log.d(TAG, "onItemClick: deviceAddress = $deviceAddress")
        //create the bond.
//NOTE: Requires API 17+? I think this is JellyBean
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.d(TAG, "Trying to pair with $deviceName")
            RFIDReader!!.createBond()
        }
    }

    /**
     * Broadcast Receiver for listing devices that are not yet paired
     * -Executed by btnDiscover() method.
     */
    private val mBroadcastReceiver1: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_FOUND) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (device.name != null && device.name.equals("RFD850019198523021230", ignoreCase = true)) {
                    RFIDReader = device
                    deviceName!!.text = RFIDReader!!.name
                    deviceAddress!!.text = RFIDReader!!.address
                    Log.d(TAG, "onReceiveRFID: " + RFIDReader!!.name + ": " + RFIDReader!!.address)
                }
            }
        }
    }
    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private val mBroadcastReceiver2: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val mDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                //3 cases:
//case1: bonded already
                if (mDevice.bondState == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.")
                }
                //case2: creating a bone
                if (mDevice.bondState == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.")
                }
                //case3: breaking a bond
                if (mDevice.bondState == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.")
                }
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: called.")
        super.onDestroy()
        unregisterReceiver(mBroadcastReceiver1)
        unregisterReceiver(mBroadcastReceiver2)
        //mBluetoothAdapter.cancelDiscovery();
    }
}