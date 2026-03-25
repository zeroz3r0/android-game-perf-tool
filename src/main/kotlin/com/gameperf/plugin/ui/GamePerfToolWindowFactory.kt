package com.gameperf.plugin.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.w.ToolWindow
import com.intellij.openapi.w.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class GamePerfToolWindowFactory : ToolWindowFactory {
    
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        val gamePerfPanel = GamePerfPanel(project)
        val content = contentFactory.createContent(gamePerfPanel, "Game Performance", false)
        toolWindow.contentManager.addContent(content)
    }
    
    override fun shouldBeAvailable(project: Project): Boolean {
        return true
    }
}
