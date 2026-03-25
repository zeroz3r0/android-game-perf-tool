package com.gameperf.plugin.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.w.ToolWindow
import com.intellij.openapi.w.ToolWindowManager
import com.intellij.ui.content.ContentFactory

class GamePerfToolWindowFactory {
    
    companion object {
        const val TOOL_WINDOW_ID = "GamePerformance"
        
        fun showToolWindow(project: Project) {
            val toolWindowManager = ToolWindowManager.getInstance(project)
            val toolWindow = toolWindowManager.getToolWindow(TOOL_WINDOW_ID)
            toolWindow?.show()
        }
    }
    
    fun createToolWindow(project: Project): ToolWindow {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID) {
            GamePerfToolWindowContent()
        }
        
        return toolWindow
    }
}

class GamePerfToolWindowContent {
    // Placeholder - will be implemented in Phase 8
}
