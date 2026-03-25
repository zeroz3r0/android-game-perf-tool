package com.gameperf.plugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.gameperf.plugin.ui.GamePerfToolWindowFactory

class GamePerfPluginListener : ProjectManagerListener {
    
    override fun projectOpened(project: Project) {
        // Auto-show tool window when project opens
        // Can be disabled by user in settings
    }
    
    override fun projectClosed(project: Project) {
        // Cleanup when project closes
    }
}
