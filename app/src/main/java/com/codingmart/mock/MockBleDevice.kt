package com.codingmart.mockito

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import kotlin.random.Random

class MockBleDevice(private val context: Context) {

    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    private var gattServer: BluetoothGattServer? = null

    private var connectedDevice: BluetoothDevice? = null
    private val handler = Handler()
    private var isSendingHeartRate = false
    private var send75 = true

    @SuppressLint("MissingPermission")
    fun startAdvertising() {
        val advertiser = bluetoothAdapter.bluetoothLeAdvertiser
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(MockBleService.SERVICE_UUID))
            .build()

        advertiser.startAdvertising(settings, data, advertiseCallback)
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.i("MockBleDevice", "Advertising started successfully")
            startGattServer()
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e("MockBleDevice", "Advertising failed with error code: $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    private fun startGattServer() {
        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        val service = android.bluetooth.BluetoothGattService(
            MockBleService.SERVICE_UUID,
            android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY
        )
        val characteristic = BluetoothGattCharacteristic(
            MockBleService.CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        service.addCharacteristic(characteristic)
        gattServer?.addService(service)
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("MockBleDevice", "Device connected: ${device?.address}")
                connectedDevice = device
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("MockBleDevice", "Device disconnected: ${device?.address}")
                connectedDevice = null
                stopHeartRateSimulation()
            }
        }

//        @SuppressLint("MissingPermission")
        @SuppressLint("MissingPermission")
        override fun onCharacteristicReadRequest(
            device: android.bluetooth.BluetoothDevice?, requestId: Int, offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            if (MockBleService.CHARACTERISTIC_UUID == characteristic?.uuid) {
                gattServer?.sendResponse(device, requestId, BluetoothGattServer.STATE_CONNECTED, 0, byteArrayOf(0x01))
            }
        }
        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            super.onNotificationSent(device, status)
            Log.i("MockBleDevice", "Notification sent to ${device?.address} with status $status")
        }
    }
    fun startHeartRateSimulation() {
        if (isSendingHeartRate) return
        isSendingHeartRate = true
        handler.post(heartRateRunnable)
    }
    private val heartRateRunnable = object : Runnable {
        override fun run() {
            if (!isSendingHeartRate) return

            val heartRate = if (send75) 75 else 65 // generate a heart rate
//            val heartRate = Random.nextInt(60, 100) // Generate a random heart rate value
            sendHeartRate(heartRate)
            send75 = !send75  // Toggle between 75 and 65

            handler.postDelayed(this, 500) // Repeat every second for 40 seconds
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendHeartRate(heartRate: Int) {
        val characteristic = gattServer?.getService(MockBleService.SERVICE_UUID)
            ?.getCharacteristic(MockBleService.CHARACTERISTIC_UUID)
        characteristic?.value = byteArrayOf(0x01, heartRate.toByte())

        connectedDevice?.let {
            gattServer?.notifyCharacteristicChanged(it, characteristic, false)
        }
    }

    fun stopHeartRateSimulation() {
        isSendingHeartRate = false
        handler.removeCallbacks(heartRateRunnable)
    }
}
