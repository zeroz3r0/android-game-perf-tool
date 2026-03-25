package com.gameperf.plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.w.ToolWindow
import com.intellij.openapi.w.ToolWindowManager
import com.intellij.ui.content.ContentFactory

class GamePerfPlugin : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        showToolWindow(project)
    }
    
    companion object {
        const val TOOL_WINDOW_ID = "Game Performance"
        
        fun showToolWindow(project: Project) {
            val toolWindowManager = ToolWindowManager.getInstance(project)
            var toolWindow = toolWindowManager.getToolWindow(TOOL_WINDOW_ID)
            
            if (toolWindow == null) {
                toolWindow = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID) {
                    val contentFactory = ApplicationManager.getApplication().getService(ContentFactory::class.java)
                    val content = contentFactory.createContent(
                        GamePerfPanel(project),
                        "Game Performance",
                        false
                    )
                    content
                }
            }
            
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
        
        // Top control panel
        add(controlPanel, java.awt.BorderLayout.NORTH)
        
        // Center: split between logs and metrics
        val centerPanel = javax.swing.JSplitPane(
            javax.swing.JSplitPane.VERTICAL_SPLIT,
            logPanel,
            metricsPanel
        )
        centerPanel.resizeWeight = 0.6
        add(centerPanel, java.awt.BorderLayout.CENTER)
        
        // Left: device selection
        add(devicePanel, java.awt.BorderLayout.WEST)
        
        // Setup control panel actions
        controlPanel.onConnect = { device ->
            logPanel.startCapture(device.id)
            metricsPanel.onDeviceConnected(device)
        }
        
        controlPanel.onDisconnect = {
            logPanel.stopCapture()
            metricsPanel.onDeviceDisconnected()
        }
        
        controlPanel.onGenerateReport = {
            metricsPanel.generateReport()
        }
    }
}

// Simple device representation (will be replaced with core.AdbConnector)
data class DeviceInfo(
    val id: String,
    val name: String,
    val model: String,
    val sdkVersion: Int,
    val isEmulator: Boolean
)
