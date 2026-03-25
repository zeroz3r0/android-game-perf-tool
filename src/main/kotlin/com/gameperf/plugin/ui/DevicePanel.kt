package com.gameperf.plugin.ui

import com.gameperf.plugin.core.AdbConnector
import javax.swing.*
import java.awt.*

class DevicePanel : JPanel() {
    
    private val adbConnector = AdbConnector()
    private val deviceList = JList<DeviceInfo>()
    private val refreshButton = JButton("Refresh")
    private val statusLabel = JLabel("No device connected")
    
    var onDeviceSelected: ((DeviceInfo) -> Unit)? = null
    
    init {
        layout = BorderLayout(5, 5)
        border = BorderFactory.createTitledBorder("Devices")
        
        // Device list
        deviceList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        deviceList.addListSelectionListener {
            if (!it.valueIsAdjusting) {
                deviceList.selectedValue?.let { device ->
                    onDeviceSelected?.invoke(device)
                }
            }
        }
        
        add(JScrollPane(deviceList), BorderLayout.CENTER)
        
        // Bottom panel with refresh
        val bottomPanel = JPanel(BorderLayout(5, 5))
        bottomPanel.add(refreshButton, BorderLayout.NORTH)
        bottomPanel.add(statusLabel, BorderLayout.SOUTH)
        add(bottomPanel, BorderLayout.SOUTH)
        
        refreshButton.addActionListener { refreshDevices() }
    }
    
    fun refreshDevices() {
        if (!adbConnector.isAdbAvailable()) {
            statusLabel.text = "ADB not available"
            statusLabel.foreground = Color.RED
            return
        }
        
        val devices = adbConnector.listDevices()
        
        if (devices.isEmpty()) {
            statusLabel.text = "No devices found"
            statusLabel.foreground = Color.ORANGE
        } else {
            statusLabel.text = "${devices.size} device(s) found"
            statusLabel.foreground = Color.GREEN
            
            val deviceInfoList = devices.map { dev ->
                DeviceInfo(
                    id = dev.id,
                    name = dev.name,
                    model = dev.model,
                    sdkVersion = dev.sdkVersion,
                    isEmulator = dev.isEmulator
                )
            }
            deviceList.model = DefaultListModel<DeviceInfo>().apply {
                deviceInfoList.forEach { addElement(it) }
            }
        }
    }
}
