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

class BluetoothManager(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val APP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val APP_NAME = "TicTacToe"
    
    private var serverSocket: BluetoothServerSocket? = null
    private var clientSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    
    var onMessageReceived: ((String) -> Unit)? = null
    var onConnectionEstablished: (() -> Unit)? = null
    var onConnectionLost: (() -> Unit)? = null
    
    private var isListening = false
    private var isConnected = false

    fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter != null
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    fun getDeviceId(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return "Unknown"
            }
        }
        return bluetoothAdapter?.address ?: "Unknown"
    }

    fun getPairedDevices(): List<BluetoothDevice> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return emptyList()
            }
        }
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }

    fun startServer() {
        if (isListening) return
        
        Thread {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return@Thread
                    }
                }
                
                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(APP_NAME, APP_UUID)
                isListening = true
                
                clientSocket = serverSocket?.accept() // Blocking call
                
                if (clientSocket != null) {
                    isConnected = true
                    inputStream = clientSocket?.inputStream
                    outputStream = clientSocket?.outputStream
                    onConnectionEstablished?.invoke()
                    startListening()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                closeConnection()
            }
        }.start()
    }

    fun connectToDevice(device: BluetoothDevice) {
        Thread {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return@Thread
                    }
                }
                
                clientSocket = device.createRfcommSocketToServiceRecord(APP_UUID)
                clientSocket?.connect() // Blocking call
                
                if (clientSocket?.isConnected == true) {
                    isConnected = true
                    inputStream = clientSocket?.inputStream
                    outputStream = clientSocket?.outputStream
                    onConnectionEstablished?.invoke()
                    startListening()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                closeConnection()
            }
        }.start()
    }

    private fun startListening() {
        Thread {
            val buffer = ByteArray(4096)
            val messageBuilder = StringBuilder()
            
            while (isConnected) {
                try {
                    val bytes = inputStream?.read(buffer) ?: -1
                    if (bytes > 0) {
                        val message = String(buffer, 0, bytes)
                        messageBuilder.append(message)
                        
                        // Check if we have a complete JSON message
                        val fullMessage = messageBuilder.toString()
                        if (fullMessage.contains("}}}")) {
                            // We have a complete message
                            val jsonMessage = fullMessage.substringBefore("}}}") + "}}}"
                            onMessageReceived?.invoke(jsonMessage)
                            
                            // Keep any remaining data for next message
                            val remaining = fullMessage.substringAfter("}}}", "")
                            messageBuilder.clear()
                            messageBuilder.append(remaining)
                        }
                    } else {
                        // Connection lost
                        isConnected = false
                        onConnectionLost?.invoke()
                        break
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    isConnected = false
                    onConnectionLost?.invoke()
                    break
                }
            }
        }.start()
    }

    fun sendMessage(message: String) {
        Thread {
            try {
                outputStream?.write(message.toByteArray())
                outputStream?.flush()
            } catch (e: IOException) {
                e.printStackTrace()
                isConnected = false
                onConnectionLost?.invoke()
            }
        }.start()
    }

    fun closeConnection() {
        isConnected = false
        isListening = false
        
        try {
            inputStream?.close()
            outputStream?.close()
            clientSocket?.close()
            serverSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        
        inputStream = null
        outputStream = null
        clientSocket = null
        serverSocket = null
    }

    fun isConnectedToDevice(): Boolean {
        return isConnected
    }
}


