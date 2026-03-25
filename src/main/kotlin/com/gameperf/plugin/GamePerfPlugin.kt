package com.gameperf.plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project

class GamePerfPlugin : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        project?.let {
            com.gameperf.plugin.ui.GamePerfToolWindowFactory.showToolWindow(it)
        }
    }
}
