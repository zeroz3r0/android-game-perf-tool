package com.gameperf.plugin.ui

import com.gameperf.plugin.core.Metric
import com.gameperf.plugin.core.MetricType
import com.gameperf.plugin.core.MetricsExtractor
import com.gameperf.plugin.core.ReportGenerator
import java.awt.*
import javax.swing.*
import javax.swing.table.DefaultTableModel

class MetricsPanel : JPanel() {
    
    private val metricsExtractor = MetricsExtractor()
    
    // Metrics displays
    private val fpsLabel = JLabel("--")
    private val minFpsLabel = JLabel("--")
    private val maxFpsLabel = JLabel("--")
    private val frameDropsLabel = JLabel("0")
    private val memoryLabel = JLabel("--")
    
    // Recent metrics table
    private val metricsTable = JTable()
    private val tableModel = DefaultTableModel(arrayOf("Type", "Value", "Time"), 0) {
        override fun isCellEditable(row: Int, column: Int) = false
    }
    
    private val warningsTextArea = JTextArea(5, 20)
    private val generateReportButton = JButton("Generate Report")
    private val exportJsonButton = JButton("Export JSON")
    
    private var currentDeviceId: String? = null
    private var warnings = mutableListOf<String>()
    
    var onReportGenerated: ((String) -> Unit)? = null
    
    init {
        layout = BorderLayout(5, 5)
        border = BorderFactory.createTitledBorder("Performance Metrics")
        
        // Top metrics panel
        val metricsGrid = JPanel(GridLayout(2, 5, 10, 5))
        metricsGrid.add(createMetricBox("FPS", fpsLabel))
        metricsGrid.add(createMetricBox("Min FPS", minFpsLabel))
        metricsGrid.add(createMetricBox("Max FPS", maxFpsLabel))
        metricsGrid.add(createMetricBox("Frame Drops", frameDropsLabel))
        metricsGrid.add(createMetricBox("Memory (MB)", memoryLabel))
        
        add(metricsGrid, BorderLayout.NORTH)
        
        // Center: recent metrics table
        metricsTable.model = tableModel
        val scrollPane = JScrollPane(metricsTable)
        scrollPane.preferredSize = Dimension(0, 200)
        add(scrollPane, BorderLayout.CENTER)
        
        // Bottom: warnings and export
        val bottomPanel = JPanel(BorderLayout(5, 5))
        
        // Warnings area
        val warningsPanel = JPanel(BorderLayout())
        warningsPanel.border = BorderFactory.createTitledBorder("Warnings & Issues")
        warningsTextArea.isEditable = false
        warningsPanel.add(JScrollPane(warningsTextArea), BorderLayout.CENTER)
        
        // Export buttons
        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 5, 5))
        buttonPanel.add(generateReportButton)
        buttonPanel.add(exportJsonButton)
        
        bottomPanel.add(warningsPanel, BorderLayout.CENTER)
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH)
        
        add(bottomPanel, BorderLayout.SOUTH)
        
        // Button actions
        generateReportButton.addActionListener { generateReport() }
        exportJsonButton.addActionListener { exportJson() }
    }
    
    private fun createMetricBox(title: String, valueLabel: JLabel): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createTitledBorder(title)
        valueLabel.font = Font("SansSerif", Font.BOLD, 24)
        valueLabel.horizontalAlignment = SwingConstants.CENTER
        panel.add(valueLabel, BorderLayout.CENTER)
        return panel
    }
    
    fun onDeviceConnected(device: DeviceInfo) {
        currentDeviceId = device.id
        clearMetrics()
    }
    
    fun onDeviceDisconnected() {
        currentDeviceId = null
    }
    
    fun addLogEntry(entry: com.gameperf.plugin.core.LogEntry) {
        val metric = metricsExtractor.extract(entry)
        
        if (metric != null) {
            SwingUtilities.invokeLater {
                tableModel.addRow(arrayOf(
                    metric.type.name,
                    formatMetricValue(metric),
                    java.text.SimpleDateFormat("HH:mm:ss")
                        .format(java.util.Date(metric.timestamp))
                ))
                
                // Keep only last 100 rows
                while (tableModel.rowCount > 100) {
                    tableModel.removeRow(0)
                }
                
                updateDisplay()
            }
        }
    }
    
    private fun formatMetricValue(metric: Metric): String {
        return when (metric.type) {
            MetricType.FPS -> "%.2f".format(metric.value)
            MetricType.FRAME_TIME -> "%.2f ms".format(metric.value)
            MetricType.MEMORY -> "%.2f MB".format(metric.value)
            MetricType.CPU -> "%.1f%%".format(metric.value)
        }
    }
    
    private fun updateDisplay() {
        metricsExtractor.getAverageFps()?.let { fpsLabel.text = "%.1f".format(it) }
        metricsExtractor.getMinFps()?.let { minFpsLabel.text = "%.1f".format(it) }
        
        val fpsMetrics = metricsExtractor.getMetricsByType(MetricType.FPS)
        if (fpsMetrics.isNotEmpty()) {
            maxFpsLabel.text = "%.1f".format(fpsMetrics.maxOf { it.value })
        }
        
        frameDropsLabel.text = metricsExtractor.getFrameDrops().toString()
        
        val memoryMetrics = metricsExtractor.getMetricsByType(MetricType.MEMORY)
        if (memoryMetrics.isNotEmpty()) {
            memoryLabel.text = "%.0f".format(memoryMetrics.map { it.value }.average())
        }
    }
    
    fun generateReport() {
        val deviceId = currentDeviceId ?: "Unknown"
        
        val reportGenerator = ReportGenerator(metricsExtractor, emptyList(), emptyList())
        val report = reportGenerator.generate(deviceId)
        val markdown = reportGenerator.toMarkdown(report)
        
        onReportGenerated?.invoke(markdown)
        
        // Show in dialog
        JOptionPane.showMessageDialog(
            this,
            JScrollPane(JTextArea(markdown, 20, 60)),
            "Performance Report",
            JOptionPane.INFORMATION_MESSAGE
        )
    }
    
    private fun exportJson() {
        val deviceId = currentDeviceId ?: "Unknown"
        
        val reportGenerator = ReportGenerator(metricsExtractor, emptyList(), emptyList())
        val report = reportGenerator.generate(deviceId)
        val json = reportGenerator.toJson(report)
        
        val fileChooser = JFileChooser()
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileChooser.selectedFile.writeText(json)
        }
    }
    
    private fun clearMetrics() {
        metricsExtractor.clear()
        tableModel.rowCount = 0
        fpsLabel.text = "--"
        minFpsLabel.text = "--"
        maxFpsLabel.text = "--"
        frameDropsLabel.text = "0"
        memoryLabel.text = "--"
        warningsTextArea.text = ""
    }
}
