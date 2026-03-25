package com.gameperf.plugin.ui

import java.awt.*
import javax.swing.*

class ControlPanel : JPanel() {
    
    private val connectButton = JButton("Connect")
    private val disconnectButton = JButton("Disconnect")
    private val startCaptureButton = JButton("Start Capture")
    private val stopCaptureButton = JButton("Stop Capture")
    private val statusLabel = JLabel("Ready")
    
    var onConnect: ((DeviceInfo) -> Unit)? = null
    var onDisconnect: (() -> Unit)? = null
    var onStartCapture: (() -> Unit)? = null
    var onStopCapture: (() -> Unit)? = null
    var onGenerateReport: (() -> Unit)? = null
    
    private var isConnected = false
    private var isCapturing = false
    
    init {
        layout = FlowLayout(FlowLayout.LEFT, 10, 5)
        border = BorderFactory.createTitledBorder("Controls")
        
        // Connection buttons
        add(connectButton)
        add(disconnectButton)
        add(JSeparator(SwingConstants.VERTICAL))
        
        // Capture buttons
        add(startCaptureButton)
        add(stopCaptureButton)
        add(JSeparator(SwingConstants.VERTICAL))
        
        // Status
        add(statusLabel)
        
        // Initial state
        disconnectButton.isEnabled = false
        startCaptureButton.isEnabled = false
        stopCaptureButton.isEnabled = false
        
        // Button actions
        connectButton.addActionListener {
            // This will be connected to DevicePanel
            statusLabel.text = "Connecting..."
        }
        
        disconnectButton.addActionListener {
            isConnected = false
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
    
    fun setConnected(device: DeviceInfo) {
        isConnected = true
        updateButtonStates()
        statusLabel.text = "Connected to ${device.name}"
    }
    
    private fun updateButtonStates() {
        connectButton.isEnabled = !isConnected
        disconnectButton.isEnabled = isConnected
        startCaptureButton.isEnabled = isConnected && !isCapturing
        stopCaptureButton.isEnabled = isConnected && isCapturing
    }
    
    fun setStatus(text: String) {
        statusLabel.text = text
    }
}
