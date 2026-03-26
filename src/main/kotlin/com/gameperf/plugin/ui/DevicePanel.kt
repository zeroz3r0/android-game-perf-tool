package com.gameperf.plugin.ui

import com.gameperf.plugin.core.AdbConnector
import com.gameperf.plugin.core.AndroidDevice
import java.awt.*
import javax.swing.*

class DevicePanel : JPanel() {
    
    private val adbConnector = AdbConnector()
    private val deviceList = JList<AndroidDevice>()
    private val refreshButton = JButton("Refresh")
    private val statusLabel = JLabel("No device connected")
    
    var onDeviceSelected: ((AndroidDevice) -> Unit)? = null
    
    init {
        layout = BorderLayout(5, 5)
        border = BorderFactory.createTitledBorder("Devices")
        
        deviceList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        deviceList.addListSelectionListener {
            if (!it.valueIsAdjusting) {
                deviceList.selectedValue?.let { device ->
                    onDeviceSelected?.invoke(device)
                }
            }
        }
        
        add(JScrollPane(deviceList), BorderLayout.CENTER)
        
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
            
            val model = DefaultListModel<AndroidDevice>()
            devices.forEach { model.addElement(it) }
            deviceList.model = model
        }
    }
}
