package com.example.tic_tac_toe

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothManager(private val appContext: Context) {
    private val btAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val SERVICE_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val SERVICE_NAME = "TicTacToe"
    
    private var listeningSocket: BluetoothServerSocket? = null
    private var activeSocket: BluetoothSocket? = null
    private var dataInputStream: InputStream? = null
    private var dataOutputStream: OutputStream? = null
    
    var onMessageReceived: ((String) -> Unit)? = null
    var onConnectionEstablished: (() -> Unit)? = null
    var onConnectionLost: (() -> Unit)? = null
    
    private var currentlyListening = false
    private var currentlyConnected = false

    fun hasBluetoothCapability(): Boolean {
        return btAdapter != null
    }

    fun bluetoothActivated(): Boolean {
        return btAdapter?.isEnabled == true
    }

    fun terminateConnection() {
        currentlyConnected = false
        currentlyListening = false
        
        try {
            dataInputStream?.close()
            dataOutputStream?.close()
            activeSocket?.close()
            listeningSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        
        dataInputStream = null
        dataOutputStream = null
        activeSocket = null
        listeningSocket = null
    }

    fun transmitMessage(messageContent: String) {
        Thread {
            try {
                dataOutputStream?.write(messageContent.toByteArray())
                dataOutputStream?.flush()
            } catch (e: IOException) {
                e.printStackTrace()
                currentlyConnected = false
                onConnectionLost?.invoke()
            }
        }.start()
    }

    private fun beginMessageListening() {
        Thread {
            val receiveBuffer = ByteArray(4096)
            val accumulatedMessage = StringBuilder()
            
            while (currentlyConnected) {
                try {
                    val bytesRead = dataInputStream?.read(receiveBuffer) ?: -1
                    if (bytesRead > 0) {
                        val receivedContent = String(receiveBuffer, 0, bytesRead)
                        accumulatedMessage.append(receivedContent)
                        
                        val completeContent = accumulatedMessage.toString()
                        if (completeContent.contains("}}}")) {
                            val extractedJson = completeContent.substringBefore("}}}") + "}}}"
                            onMessageReceived?.invoke(extractedJson)
                            
                            val leftoverData = completeContent.substringAfter("}}}", "")
                            accumulatedMessage.clear()
                            accumulatedMessage.append(leftoverData)
                        }
                    } else {
                        currentlyConnected = false
                        onConnectionLost?.invoke()
                        break
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    currentlyConnected = false
                    onConnectionLost?.invoke()
                    break
                }
            }
        }.start()
    }

    fun establishClientConnection(targetDevice: BluetoothDevice) {
        Thread {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(
                            appContext,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return@Thread
                    }
                }
                
                activeSocket = targetDevice.createRfcommSocketToServiceRecord(SERVICE_UUID)
                activeSocket?.connect()
                
                if (activeSocket?.isConnected == true) {
                    currentlyConnected = true
                    dataInputStream = activeSocket?.inputStream
                    dataOutputStream = activeSocket?.outputStream
                    onConnectionEstablished?.invoke()
                    beginMessageListening()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                terminateConnection()
            }
        }.start()
    }

    fun initiateServerMode() {
        if (currentlyListening) return
        
        Thread {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(
                            appContext,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return@Thread
                    }
                }
                
                listeningSocket = btAdapter?.listenUsingRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID)
                currentlyListening = true
                
                activeSocket = listeningSocket?.accept()
                
                if (activeSocket != null) {
                    currentlyConnected = true
                    dataInputStream = activeSocket?.inputStream
                    dataOutputStream = activeSocket?.outputStream
                    onConnectionEstablished?.invoke()
                    beginMessageListening()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                terminateConnection()
            }
        }.start()
    }

    fun retrievePairedDeviceList(): List<BluetoothDevice> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return emptyList()
            }
        }
        return btAdapter?.bondedDevices?.toList() ?: emptyList()
    }

    fun retrieveDeviceIdentifier(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return "Unknown"
            }
        }
        return btAdapter?.address ?: "Unknown"
    }

    fun verifyConnectionStatus(): Boolean {
        return currentlyConnected
    }
    
    fun resetConnectionState() {
        currentlyConnected = false
        currentlyListening = false
    }
}
