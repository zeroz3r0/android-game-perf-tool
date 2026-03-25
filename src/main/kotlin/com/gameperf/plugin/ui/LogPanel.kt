package com.gameperf.plugin.ui

import com.gameperf.plugin.core.LogEntry
import com.gameperf.plugin.core.LogcatReader
import com.gameperf.plugin.core.LogLevel
import java.awt.*
import javax.swing.*
import javax.swing.table.DefaultTableModel

class LogPanel : JPanel() {
    
    private val logTable = JTable()
    private val logScrollPane = JScrollPane(logTable)
    private val filterField = JTextField(20)
    private val levelFilter = JComboBox<LogLevel?>(arrayOf(null, LogLevel.DEBUG, LogLevel.INFO, LogLevel.WARN, LogLevel.ERROR))
    private val clearButton = JButton("Clear")
    private val pauseButton = JToggleButton("Pause")
    
    private var logcatReader: LogcatReader? = null
    private var isPaused = false
    private val logEntries = mutableListOf<LogEntry>()
    private val tableModel = DefaultTableModel(arrayOf("Time", "Level", "Tag", "Message"), 0) {
        override fun isCellEditable(row: Int, column: Int) = false
    }
    
    init {
        layout = BorderLayout(5, 5)
        border = BorderFactory.createTitledBorder("Logcat")
        
        // Table setup
        logTable.model = tableModel
        logTable.autoResizeMode = JTable.AUTO_RESIZE_OFF
        logTable.columnModel.getColumn(0).preferredWidth = 80
        logTable.columnModel.getColumn(1).preferredWidth = 50
        logTable.columnModel.getColumn(2).preferredWidth = 100
        logTable.columnModel.getColumn(3).preferredWidth = 400
        
        // Top filter panel
        val filterPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 5))
        filterPanel.add(JLabel("Filter:"))
        filterPanel.add(filterField)
        filterPanel.add(JLabel("Level:"))
        filterPanel.add(levelFilter)
        filterPanel.add(clearButton)
        filterPanel.add(pauseButton)
        
        add(filterPanel, BorderLayout.NORTH)
        add(logScrollPane, BorderLayout.CENTER)
        
        // Button actions
        clearButton.addActionListener { clearLogs() }
        pauseButton.addActionListener { isPaused = !isPaused }
        filterField.addActionListener { applyFilter() }
    }
    
    fun startCapture(deviceId: String) {
        clearLogs()
        
        logcatReader = LogcatReader(deviceId) { entry ->
            if (!isPaused) {
                addLogEntry(entry)
            }
        }
        logcatReader?.start()
    }
    
    fun stopCapture() {
        logcatReader?.stop()
        logcatReader = null
    }
    
    private fun addLogEntry(entry: LogEntry) {
        logEntries.add(entry)
        
        // Apply filter
        val filter = filterField.text
        val selectedLevel = levelFilter.selectedItem as? LogLevel
        
        if (selectedLevel != null && entry.level.ordinal < selectedLevel.ordinal) {
            return
        }
        
        if (filter.isNotEmpty() && !entry.message.contains(filter, ignoreCase = true) 
            && !entry.tag.contains(filter, ignoreCase = true)) {
            return
        }
        
        SwingUtilities.invokeLater {
            val time = java.text.SimpleDateFormat("HH:mm:ss.SSS")
                .format(java.util.Date(entry.timestamp))
            
            tableModel.addRow(arrayOf(
                time,
                entry.level.name,
                entry.tag,
                entry.message
            ))
            
            // Auto-scroll to bottom
            val rowCount = tableModel.rowCount
            if (rowCount > 0) {
                logTable.scrollRectToVisible(logTable.getCellRect(rowCount - 1, 0, true))
            }
        }
    }
    
    private fun clearLogs() {
        logEntries.clear()
        tableModel.rowCount = 0
    }
    
    private fun applyFilter() {
        // Re-apply filter to existing entries
        val filter = filterField.text
        tableModel.rowCount = 0
        
        for (entry in logEntries) {
            if (filter.isEmpty() || entry.message.contains(filter, ignoreCase = true) 
                || entry.tag.contains(filter, ignoreCase = true)) {
                addLogEntry(entry)
            }
        }
    }
    
    fun getLogEntries(): List<LogEntry> = logEntries.toList()
}
