package com.gameperf.plugin.ui

import com.gameperf.plugin.core.AndroidDevice
import java.awt.*
import javax.swing.*

class ControlPanel : JPanel() {
    
    private val connectButton = JButton("Connect")
    private val disconnectButton = JButton("Disconnect")
    private val startCaptureButton = JButton("Start Capture")
    private val stopCaptureButton = JButton("Stop Capture")
    private val statusLabel = JLabel("Ready")
    
    var onConnect: ((AndroidDevice) -> Unit)? = null
    var onDisconnect: (() -> Unit)? = null
    var onStartCapture: (() -> Unit)? = null
    var onStopCapture: (() -> Unit)? = null
    var onGenerateReport: (() -> Unit)? = null
    
    private var isConnected = false
    private var isCapturing = false
    private var selectedDevice: AndroidDevice? = null
    
    init {
        layout = FlowLayout(FlowLayout.LEFT, 10, 5)
        border = BorderFactory.createTitledBorder("Controls")
        
        add(connectButton)
        add(disconnectButton)
        add(JSeparator(SwingConstants.VERTICAL))
        
        add(startCaptureButton)
        add(stopCaptureButton)
        add(JSeparator(SwingConstants.VERTICAL))
        
        add(statusLabel)
        
        connectButton.isEnabled = false
        disconnectButton.isEnabled = false
        startCaptureButton.isEnabled = false
        stopCaptureButton.isEnabled = false
        
        connectButton.addActionListener {
            val device = selectedDevice ?: return@addActionListener
            isConnected = true
            updateButtonStates()
            statusLabel.text = "Connected to ${device.model}"
            onConnect?.invoke(device)
        }
        
        disconnectButton.addActionListener {
            isConnected = false
            isCapturing = false
            updateButtonStates()
            statusLabel.text = "Disconnected"
            onDisconnect?.invoke()
        }
        
        startCaptureButton.addActionListener {
            isCapturing = true
            updateButtonStates()
            statusLabel.text = "Capturing..."
            onStartCapture?.invoke()
        }
        
        stopCaptureButton.addActionListener {
            isCapturing = false
            updateButtonStates()
            statusLabel.text = "Stopped"
            onStopCapture?.invoke()
        }
    }
    
    fun setSelectedDevice(device: AndroidDevice) {
        selectedDevice = device
        if (!isConnected) {
            connectButton.isEnabled = true
            statusLabel.text = "Device selected: ${device.model}"
        }
    }
    
    fun setConnected(device: AndroidDevice) {
        isConnected = true
        updateButtonStates()
        statusLabel.text = "Connected to ${device.model}"
    }
    
    private fun updateButtonStates() {
        connectButton.isEnabled = !isConnected && selectedDevice != null
        disconnectButton.isEnabled = isConnected
        startCaptureButton.isEnabled = isConnected && !isCapturing
        stopCaptureButton.isEnabled = isConnected && isCapturing
    }
    
    fun setStatus(text: String) {
        statusLabel.text = text
    }
}
