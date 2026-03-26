package com.gameperf.plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.gameperf.plugin.ui.ControlPanel
import com.gameperf.plugin.ui.DevicePanel
import com.gameperf.plugin.ui.LogPanel
import com.gameperf.plugin.ui.MetricsPanel

class GamePerfPlugin : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        showToolWindow(project)
    }
    
    companion object {
        const val TOOL_WINDOW_ID = "GamePerformance"
        
        fun showToolWindow(project: Project) {
            val toolWindow = ToolWindowManager.getInstance(project)
                .getToolWindow(TOOL_WINDOW_ID) ?: return
            toolWindow.show()
        }
    }
}

class GamePerfPanel(private val project: Project) : javax.swing.JPanel() {
    
    private val devicePanel = DevicePanel()
    private val logPanel = LogPanel()
    private val metricsPanel = MetricsPanel()
    private val controlPanel = ControlPanel()
    
    init {
        layout = java.awt.BorderLayout(10, 10)
        
        add(controlPanel, java.awt.BorderLayout.NORTH)
        
        val centerPanel = javax.swing.JSplitPane(
            javax.swing.JSplitPane.VERTICAL_SPLIT,
            logPanel,
            metricsPanel
        )
        centerPanel.resizeWeight = 0.6
        add(centerPanel, java.awt.BorderLayout.CENTER)
        
        add(devicePanel, java.awt.BorderLayout.WEST)
        
        // Wire LogPanel -> MetricsPanel: each log entry feeds the metrics extractor
        logPanel.onLogEntry = { entry ->
            metricsPanel.addLogEntry(entry)
        }
        
        // Wire DevicePanel selection -> ControlPanel knows which device to connect
        devicePanel.onDeviceSelected = { device ->
            controlPanel.setSelectedDevice(device)
        }
        
        controlPanel.onConnect = { device ->
            logPanel.startCapture(device.id)
            metricsPanel.onDeviceConnected(device)
        }
        
        controlPanel.onDisconnect = {
            logPanel.stopCapture()
            metricsPanel.onDeviceDisconnected()
        }
    }
}
